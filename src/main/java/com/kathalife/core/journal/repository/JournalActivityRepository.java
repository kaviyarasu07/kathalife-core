package com.kathalife.core.journal.repository;

import com.kathalife.core.journal.entity.JournalActivity;
import com.kathalife.core.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalActivityRepository extends JpaRepository<JournalActivity, UUID> {

    Optional<JournalActivity> findByUserAndActivityDateAndDeletedAtIsNull(
            User user,
            LocalDate activityDate
    );

    List<JournalActivity> findByUserAndActivityDateBetweenAndDeletedAtIsNull(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<JournalActivity> findByIdAndUserAndDeletedAtIsNull(
            UUID id,
            User user
    );

    @Query("SELECT j FROM JournalActivity j " +
            "WHERE j.user = :user " +
            "AND j.deletedAt IS NOT NULL " +
            "AND j.deletedAt > :cutoffDate " +
            "ORDER BY j.deletedAt DESC")
    List<JournalActivity> findDeletedWithinWindow(
            @Param("user") User user,
            @Param("cutoffDate") LocalDateTime cutoffDate
    );

    boolean existsByUserAndActivityDateBetweenAndStoryLockedTrue(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );
}
