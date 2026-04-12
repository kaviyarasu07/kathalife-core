package com.kathalife.core.user.repository;

import com.kathalife.core.user.entity.User;
import com.kathalife.core.user.entity.UserLifeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserLifeSummaryRepository extends JpaRepository<UserLifeSummary, UUID> {
    Optional<UserLifeSummary> findByUserId(UUID userId);
    Optional<UserLifeSummary> findByUser(User user);
}
