package com.kathalife.core.journal.dto;

import java.time.LocalDate;

public record ActivityRequest(
    String content,
    LocalDate activityDate
) {}
