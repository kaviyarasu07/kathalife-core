package com.kathalife.core.user.repository;

import com.kathalife.core.user.entity.BioProfile;
import com.kathalife.core.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BioProfileRepository extends JpaRepository<BioProfile, UUID> {
    Optional<BioProfile> findByUserId(UUID userId);
    Optional<BioProfile> findByUser(User user);
}
