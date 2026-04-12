package com.kathalife.core.journal.service;

import com.kathalife.core.journal.dto.ActivityRequest;
import com.kathalife.core.journal.dto.ActivityResponse;
import com.kathalife.core.journal.dto.WeekActivitiesResponse;
import com.kathalife.core.user.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JournalService {

    ActivityResponse saveActivity(User currentUser, ActivityRequest request);

    ActivityResponse getActivityByDate(User currentUser, LocalDate date);

    WeekActivitiesResponse getWeekActivities(User currentUser, LocalDate weekStart);

    ActivityResponse getActivityById(User currentUser, UUID activityId);

    void deleteActivity(User currentUser, UUID activityId);

    List<ActivityResponse> getDeletedActivities(User currentUser);

    ActivityResponse restoreActivity(User currentUser, UUID activityId);
}
