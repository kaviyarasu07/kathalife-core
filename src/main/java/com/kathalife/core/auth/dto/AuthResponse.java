package com.kathalife.core.auth.dto;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    boolean bioCompleted,
    UUID userId,
    String email
) {}
