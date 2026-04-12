package com.kathalife.core.journal.dto;

import java.time.LocalDate;

public record DayEntryResponse(
    LocalDate date,
    String dayOfWeek,
    ActivityResponse entry
) {}
