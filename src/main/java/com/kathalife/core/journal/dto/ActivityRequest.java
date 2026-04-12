package com.kathalife.core.journal.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record ActivityRequest(
    @NotBlank(message = "Content is required")
    String content,

    LocalDate activityDate
) {}
