package com.kathalife.core.journal.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityResponse(
    UUID id,
    String content,
    LocalDate activityDate,
    String sttStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
