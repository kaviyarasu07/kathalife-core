package com.kathalife.core.user.dto;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String languagePref,
    Boolean isActive,
    boolean bioCompleted
) {}
