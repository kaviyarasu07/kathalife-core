package com.kathalife.core.journal.controller;

import com.kathalife.core.common.response.ApiResponse;
import com.kathalife.core.journal.dto.ActivityRequest;
import com.kathalife.core.journal.dto.ActivityResponse;
import com.kathalife.core.journal.dto.WeekActivitiesResponse;
import com.kathalife.core.journal.service.JournalService;
import com.kathalife.core.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/journal")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Journal", description = "Journal activity management APIs")
public class JournalController {

    private final JournalService journalService;

    @PostMapping("/activities")
    @Operation(summary = "Save journal entry for a date (upsert)")
    public ResponseEntity<ApiResponse<ActivityResponse>> saveActivity(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ActivityRequest request) {

        ActivityResponse response = journalService.saveActivity(currentUser, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response, "Activity saved successfully"));
    }

    @GetMapping("/activities")
    @Operation(summary = "Get journal entry for a date")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivityByDate(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();

        ActivityResponse response = journalService.getActivityByDate(currentUser, targetDate);

        if (response == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No entry for this date"));
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/activities/week")
    @Operation(summary = "Get week activities grouped by day")
    public ResponseEntity<ApiResponse<WeekActivitiesResponse>> getWeekActivities(
            @AuthenticationPrincipal User currentUser,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart) {

        WeekActivitiesResponse response = journalService.getWeekActivities(currentUser, weekStart);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "Get single activity by id")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivityById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        ActivityResponse response = journalService.getActivityById(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/activities/{id}")
    @Operation(summary = "Soft delete activity")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        journalService.deleteActivity(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Activity deleted successfully"));
    }

    @GetMapping("/activities/deleted")
    @Operation(summary = "Get recoverable deleted activities")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getDeletedActivities(
            @AuthenticationPrincipal User currentUser) {

        List<ActivityResponse> response = journalService.getDeletedActivities(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/activities/{id}/restore")
    @Operation(summary = "Restore soft deleted activity")
    public ResponseEntity<ApiResponse<ActivityResponse>> restoreActivity(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        ActivityResponse response = journalService.restoreActivity(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success(response, "Activity restored successfully"));
    }
}
