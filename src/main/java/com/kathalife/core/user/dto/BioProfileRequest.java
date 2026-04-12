package com.kathalife.core.user.dto;

import java.time.LocalDate;

public record BioProfileRequest(
    String fullName,
    LocalDate dateOfBirth,
    String hometown,
    String occupation,
    String familyNotes,
    String languagePref
) {}
