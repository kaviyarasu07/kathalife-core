package com.kathalife.core.user.service;

import com.kathalife.core.user.dto.*;
import com.kathalife.core.user.entity.BioProfile;
import com.kathalife.core.user.entity.User;
import com.kathalife.core.user.entity.UserLifeSummary;
import com.kathalife.core.user.repository.BioProfileRepository;
import com.kathalife.core.user.repository.UserLifeSummaryRepository;
import com.kathalife.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BioProfileRepository bioProfileRepository;
    private final UserLifeSummaryRepository userLifeSummaryRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(User currentUser) {
        Optional<BioProfile> bioOpt = bioProfileRepository.findByUser(currentUser);

        boolean bioCompleted = bioOpt.isPresent()
                && bioOpt.get().getFullName() != null
                && !bioOpt.get().getFullName().isBlank();

        return new UserResponse(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getLanguagePref(),
                currentUser.getIsActive(),
                bioCompleted
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BioProfileResponse getBioProfile(User currentUser) {
        Optional<BioProfile> bioOpt = bioProfileRepository.findByUser(currentUser);

        if (bioOpt.isEmpty()) {
            return new BioProfileResponse(null, null, null, null, null, null, null, currentUser.getLanguagePref(), null);
        }

        BioProfile bio = bioOpt.get();
        return new BioProfileResponse(
                bio.getId(),
                bio.getFullName(),
                bio.getDateOfBirth(),
                bio.getHometown(),
                bio.getOccupation(),
                bio.getFamilyNotes(),
                bio.getProfilePicUrl(),
                currentUser.getLanguagePref(),
                bio.getUpdatedAt()
        );
    }

    @Override
    public BioProfileResponse updateBioProfile(User currentUser, BioProfileRequest request) {
        BioProfile bio = bioProfileRepository.findByUser(currentUser).orElseGet(() -> {
            BioProfile newBio = new BioProfile();
            newBio.setUser(currentUser);
            return newBio;
        });
        
        if (request.languagePref() != null && !request.languagePref().isBlank()) {
            currentUser.setLanguagePref(request.languagePref());
            userRepository.save(currentUser);
            log.info("Language preference updated to: {} for user: {}",
                    request.languagePref(), currentUser.getEmail());
        }
        
        if (request.fullName() != null) bio.setFullName(request.fullName());
        if (request.dateOfBirth() != null) bio.setDateOfBirth(request.dateOfBirth());
        if (request.hometown() != null) bio.setHometown(request.hometown());
        if (request.occupation() != null) bio.setOccupation(request.occupation());
        if (request.familyNotes() != null) bio.setFamilyNotes(request.familyNotes());

        BioProfile saved = bioProfileRepository.save(bio);

        log.info("Bio profile updated for user id: {}", currentUser.getId());

        return new BioProfileResponse(
                saved.getId(),
                saved.getFullName(),
                saved.getDateOfBirth(),
                saved.getHometown(),
                saved.getOccupation(),
                saved.getFamilyNotes(),
                saved.getProfilePicUrl(),
                currentUser.getLanguagePref(),
                saved.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LifeSummaryResponse getLifeSummary(User currentUser) {
        Optional<UserLifeSummary> summaryOpt = userLifeSummaryRepository.findByUser(currentUser);

        if (summaryOpt.isEmpty()) {
            return new LifeSummaryResponse(null, null, null);
        }

        UserLifeSummary summary = summaryOpt.get();
        return new LifeSummaryResponse(
                summary.getId(),
                summary.getSummaryText(),
                summary.getUpdatedAt()
        );
    }

    @Override
    public LifeSummaryResponse updateLifeSummary(User currentUser, LifeSummaryRequest request) {
        UserLifeSummary summary = userLifeSummaryRepository.findByUser(currentUser).orElseGet(() -> {
            UserLifeSummary newSummary = new UserLifeSummary();
            newSummary.setUser(currentUser);
            return newSummary;
        });

        summary.setSummaryText(request.summaryText());

        UserLifeSummary saved = userLifeSummaryRepository.save(summary);
        log.info("Life summary updated for user id: {}", currentUser.getId());

        return new LifeSummaryResponse(
                saved.getId(),
                saved.getSummaryText(),
                saved.getUpdatedAt()
        );
    }
}
