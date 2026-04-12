package com.kathalife.core.auth.repository;

import com.kathalife.core.auth.entity.PasswordChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordChangeRequestRepository extends JpaRepository<PasswordChangeRequest, UUID> {
    Optional<PasswordChangeRequest> findByEmailAndOtpAndStatus(String email, String otp, PasswordChangeRequest.RequestStatus status);

    Optional<PasswordChangeRequest> findByEmailAndStatusAndValidToAfter(
            String email,
            PasswordChangeRequest.RequestStatus status,
            LocalDateTime validTo
    );

    @Modifying
    @Query("UPDATE PasswordChangeRequest p SET p.status = 'EXPIRED' WHERE p.email = :email AND p.status = 'GENERATED'")
    void expireAllPendingForEmail(@Param("email") String email);
}
