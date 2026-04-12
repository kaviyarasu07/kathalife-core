package com.kathalife.core.journal.dto;

import java.time.LocalDate;
import java.util.List;

public record WeekActivitiesResponse(
    LocalDate weekStart,
    LocalDate weekEnd,
    int totalEntries,
    boolean storyGenerated,
    List<DayEntryResponse> days
) {}
