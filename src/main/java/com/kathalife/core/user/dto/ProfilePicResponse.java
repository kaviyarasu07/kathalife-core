package com.kathalife.core.user.dto;

import java.util.UUID;

public record ProfilePicResponse(
    UUID userId,
    String profilePicUrl
) {}
