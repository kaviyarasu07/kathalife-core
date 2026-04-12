package com.kathalife.core.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LifeSummaryResponse(
    UUID id,
    String summaryText,
    LocalDateTime updatedAt
) {}
