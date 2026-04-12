package com.kathalife.core.journal.service;

import com.kathalife.core.common.exception.ActivityLockedException;
import com.kathalife.core.common.exception.ResourceNotFoundException;
import com.kathalife.core.journal.dto.ActivityRequest;
import com.kathalife.core.journal.dto.ActivityResponse;
import com.kathalife.core.journal.dto.DayEntryResponse;
import com.kathalife.core.journal.dto.WeekActivitiesResponse;
import com.kathalife.core.journal.entity.JournalActivity;
import com.kathalife.core.journal.entity.JournalActivity.SttStatus;
import com.kathalife.core.journal.repository.JournalActivityRepository;
import com.kathalife.core.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final JournalActivityRepository journalActivityRepository;

    private ActivityResponse toResponse(JournalActivity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getContent(),
                activity.getActivityDate(),
                activity.getSttStatus().name(),
                activity.getStoryLocked(),
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }

    @Override
    public ActivityResponse saveActivity(User currentUser, ActivityRequest request) {
        LocalDate activityDate = request.activityDate() != null
                ? request.activityDate()
                : LocalDate.now();

        Optional<JournalActivity> existing = journalActivityRepository
                .findByUserAndActivityDateAndDeletedAtIsNull(currentUser, activityDate);

        if (existing.isPresent()) {
            JournalActivity activity = existing.get();

            if (activity.getStoryLocked()) {
                throw new ActivityLockedException("This entry is locked after story generation");
            }

            activity.setContent(request.content());
            JournalActivity saved = journalActivityRepository.save(activity);
            log.info("Activity updated for user: {} date: {}", currentUser.getEmail(), activityDate);
            return toResponse(saved);
        }

        JournalActivity activity = new JournalActivity();
        activity.setUser(currentUser);
        activity.setContent(request.content());
        activity.setActivityDate(activityDate);
        activity.setSttStatus(SttStatus.NONE);
        activity.setStoryLocked(false);

        JournalActivity saved = journalActivityRepository.save(activity);
        log.info("Activity created for user: {} date: {}", currentUser.getEmail(), activityDate);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityByDate(User currentUser, LocalDate date) {
        Optional<JournalActivity> activity = journalActivityRepository
                .findByUserAndActivityDateAndDeletedAtIsNull(currentUser, date);

        if (activity.isEmpty()) {
            return null;
        }

        return toResponse(activity.get());
    }

    @Override
    @Transactional(readOnly = true)
    public WeekActivitiesResponse getWeekActivities(User currentUser, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);

        List<JournalActivity> weekActivities = journalActivityRepository
                .findByUserAndActivityDateBetweenAndDeletedAtIsNull(currentUser, weekStart, weekEnd);

        boolean storyGenerated = journalActivityRepository
                .existsByUserAndActivityDateBetweenAndStoryLockedTrue(currentUser, weekStart, weekEnd);

        Map<LocalDate, JournalActivity> entryByDate = weekActivities.stream()
                .collect(Collectors.toMap(
                        JournalActivity::getActivityDate,
                        a -> a
                ));

        List<DayEntryResponse> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            JournalActivity dayActivity = entryByDate.get(day);
            days.add(new DayEntryResponse(
                    day,
                    day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    dayActivity != null ? toResponse(dayActivity) : null
            ));
        }

        return new WeekActivitiesResponse(
                weekStart,
                weekEnd,
                weekActivities.size(),
                storyGenerated,
                days
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(User currentUser, UUID activityId) {
        JournalActivity activity = journalActivityRepository
                .findByIdAndUserAndDeletedAtIsNull(activityId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        return toResponse(activity);
    }

    @Override
    public void deleteActivity(User currentUser, UUID activityId) {
        JournalActivity activity = journalActivityRepository
                .findByIdAndUserAndDeletedAtIsNull(activityId, currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        if (activity.getStoryLocked()) {
            throw new ActivityLockedException("Cannot delete a locked entry");
        }

        activity.setDeletedAt(LocalDateTime.now());
        journalActivityRepository.save(activity);

        log.info("Activity soft deleted: {} by user: {}", activityId, currentUser.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getDeletedActivities(User currentUser) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        List<JournalActivity> deleted = journalActivityRepository
                .findDeletedWithinWindow(currentUser, cutoffDate);

        return deleted.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ActivityResponse restoreActivity(User currentUser, UUID activityId) {
        JournalActivity activity = journalActivityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        if (!activity.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Activity not found");
        }

        if (activity.getDeletedAt() == null) {
            throw new ResourceNotFoundException("Activity is not deleted");
        }

        if (activity.getDeletedAt().isBefore(LocalDateTime.now().minusDays(30))) {
            throw new ResourceNotFoundException("Recovery window expired for this entry");
        }

        activity.setDeletedAt(null);
        JournalActivity saved = journalActivityRepository.save(activity);

        log.info("Activity restored: {} by user: {}", activityId, currentUser.getEmail());

        return toResponse(saved);
    }
}
