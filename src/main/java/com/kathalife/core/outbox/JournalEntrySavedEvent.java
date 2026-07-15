package com.kathalife.core.outbox;

import java.time.LocalDate;
import java.util.UUID;

public record JournalEntrySavedEvent(
    UUID journalEntryId,
    UUID userId,
    String fullText,
    LocalDate activityDate,
    String language
) {}
