package com.kathalife.core.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LifeSummaryRequest(
    @NotBlank(message = "Summary text is required")
    String summaryText
) {}
