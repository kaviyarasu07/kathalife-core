package com.kathalife.core.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BioProfileResponse(
    UUID id,
    String fullName,
    LocalDate dateOfBirth,
    String hometown,
    String occupation,
    String familyNotes,
    String profilePicUrl,
    String languagePref,
    LocalDateTime updatedAt
) {}
