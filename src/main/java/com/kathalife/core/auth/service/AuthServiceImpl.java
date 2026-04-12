package com.kathalife.core.auth.service;

import com.kathalife.core.auth.dto.*;
import com.kathalife.core.auth.entity.PasswordChangeRequest;
import com.kathalife.core.auth.entity.RefreshToken;
import com.kathalife.core.auth.repository.PasswordChangeRequestRepository;
import com.kathalife.core.auth.repository.RefreshTokenRepository;
import com.kathalife.core.common.exception.DuplicateEmailException;
import com.kathalife.core.common.exception.InvalidTokenException;
import com.kathalife.core.common.exception.ResourceNotFoundException;
import com.kathalife.core.user.entity.BioProfile;
import com.kathalife.core.user.entity.User;
import com.kathalife.core.user.repository.BioProfileRepository;
import com.kathalife.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BioProfileRepository bioProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordChangeRequestRepository passwordChangeRequestRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @org.springframework.beans.factory.annotation.Value("${application.jwt.expiration-ms}")
    private long expirationMs;

    @org.springframework.beans.factory.annotation.Value("${application.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        BioProfile bioProfile = new BioProfile();
        bioProfile.setUser(savedUser);
        bioProfileRepository.save(bioProfile);

        String accessToken = jwtService.generateAccessToken(savedUser);
        String rawRefreshToken = jwtService.generateRefreshToken(savedUser);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(savedUser);
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        log.info("Welcome email sent to: {}", savedUser.getEmail());

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                expirationMs,
                false,
                savedUser.getId(),
                savedUser.getEmail()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean bioCompleted = user.getBioProfile() != null
                && user.getBioProfile().getFullName() != null
                && !user.getBioProfile().getFullName().isBlank();

        refreshTokenRepository.deleteAllByUserIdAndRevokedFalse(user.getId());

        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                "Bearer",
                expirationMs,
                bioCompleted,
                user.getId(),
                user.getEmail()
        );
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        if (!userRepository.existsByEmail(request.email())) {
            log.info("Forgot password requested for unregistered email: {}", request.email());
            return;
        }

        Optional<PasswordChangeRequest> existingOtp =
                passwordChangeRequestRepository.findByEmailAndStatusAndValidToAfter(
                        request.email(),
                        PasswordChangeRequest.RequestStatus.GENERATED,
                        LocalDateTime.now()
                );

        if (existingOtp.isPresent()) {
            log.info("Valid OTP already exists for: {} Reusing existing OTP.", request.email());
            return;
        }

        passwordChangeRequestRepository.expireAllPendingForEmail(request.email());

        String otp = "123456";

        PasswordChangeRequest pcr = new PasswordChangeRequest();
        pcr.setEmail(request.email());
        pcr.setOtp(hashToken(otp));
        pcr.setStatus(PasswordChangeRequest.RequestStatus.GENERATED);
        pcr.setValidTo(LocalDateTime.now().plusMinutes(15));

        passwordChangeRequestRepository.save(pcr);

        log.info("OTP sent to: {}", request.email());
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new InvalidTokenException("Passwords do not match");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PasswordChangeRequest pcr = passwordChangeRequestRepository
                .findByEmailAndOtpAndStatus(
                        request.email(),
                        hashToken(request.otp()),
                        PasswordChangeRequest.RequestStatus.GENERATED
                ).orElseThrow(() -> new InvalidTokenException("Invalid or expired OTP"));

        if (pcr.getValidTo().isBefore(LocalDateTime.now())) {
            pcr.setStatus(PasswordChangeRequest.RequestStatus.EXPIRED);
            passwordChangeRequestRepository.save(pcr);
            throw new InvalidTokenException("OTP has expired");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        pcr.setStatus(PasswordChangeRequest.RequestStatus.CHANGED);
        passwordChangeRequestRepository.save(pcr);

        refreshTokenRepository.deleteAllByUserId(user.getId());

        log.info("Password reset successful for: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String email = jwtService.extractEmail(request.refreshToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHash(hashToken(request.refreshToken()))
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (storedToken.getRevoked()) {
            throw new InvalidTokenException("Refresh token revoked");
        }
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        boolean bioCompleted = user.getBioProfile() != null
                && user.getBioProfile().getFullName() != null
                && !user.getBioProfile().getFullName().isBlank();

        return new AuthResponse(
                newAccessToken,
                request.refreshToken(),
                "Bearer",
                expirationMs,
                bioCompleted,
                user.getId(),
                user.getEmail()
        );
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}
