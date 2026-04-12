package com.kathalife.core.auth.repository;

import com.kathalife.core.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteAllByUserId(UUID userId);
    void deleteAllByUserIdAndRevokedFalse(UUID userId);
}
