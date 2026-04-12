# Journal API Documentation

This document describes the endpoints and internal business logic of the Journal Module in the KathaLife Core application. All endpoints require a valid JWT Bearer token in the `Authorization` header.

## Base URL
`/api/v1/journal`

---

## 1. Save Journal Entry (Upsert)

**Endpoint:** `POST /activities`  
**Summary:** Creates or updates a journal entry for a specific date. Only one entry is permitted per day per user.

### Request Body (`ActivityRequest`)
```json
{
  "content": "Today I started learning Spring Boot...",
  "activityDate": "2026-04-12"
}
```
*Note: `activityDate` is optional. If not provided, it defaults to the current date.*

### Response Body (`ApiResponse<ActivityResponse>`)
```json
{
  "success": true,
  "message": "Activity saved successfully",
  "data": {
    "id": "c3d4e5f6-a7b8-c9d0-e1f2-a3b4c5d6e7f8",
    "content": "Today I started learning Spring Boot...",
    "activityDate": "2026-04-12",
    "sttStatus": "NONE",
    "storyLocked": false,
    "createdAt": "2026-04-12T10:00:00",
    "updatedAt": "2026-04-12T10:00:00"
  },
  "timestamp": "2026-04-12T10:05:00"
}
```

### Business Logic
1. **Validation**: The `@Valid` annotation ensures the `content` field is not blank.
2. **Date Resolution**: Uses the provided `activityDate` or defaults to today's date.
3. **Data Retrieval**: Checks if an active (non-deleted) entry already exists for the given user and date.
4. **Update Flow (if exists)**:
   - Checks the `storyLocked` flag. If `true`, throws an `ActivityLockedException` (423 Locked).
   - If not locked, updates the `content` of the existing entry.
5. **Create Flow (if not exists)**:
   - Creates a new `JournalActivity` entity.
   - Sets `sttStatus` to `NONE` and `storyLocked` to `false`.
6. **Persistence**: Saves the entity (which updates the `@LastModifiedDate`).
7. **Response Mapping**: Returns the saved entity mapped to an `ActivityResponse`.

---

## 2. Get Journal Entry by Date

**Endpoint:** `GET /activities`  
**Summary:** Retrieves a single journal entry for a specific date.

### Request Parameters
- `date` (optional): The target date in ISO format (e.g., `2026-04-12`). Defaults to today if omitted.

### Response Body (`ApiResponse<ActivityResponse>`)
*If an entry exists:*
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "c3d4e5f6-a7b8-c9d0-e1f2-a3b4c5d6e7f8",
    "content": "Today I started learning Spring Boot...",
    "activityDate": "2026-04-12",
    "sttStatus": "NONE",
    "storyLocked": false,
    "createdAt": "2026-04-12T10:00:00",
    "updatedAt": "2026-04-12T10:00:00"
  },
  "timestamp": "2026-04-12T10:10:00"
}
```
*If no entry exists for the date:*
```json
{
  "success": true,
  "message": "No entry for this date",
  "data": null,
  "timestamp": "2026-04-12T10:10:00"
}
```

### Business Logic
1. **Date Resolution**: Determines the target date based on the query parameter.
2. **Data Retrieval**: Queries the database for an active entry matching the user and date.
3. **Response Mapping**: Returns the mapped `ActivityResponse` or `null` gracefully if no entry is found.

---

## 3. Get Week Activities

**Endpoint:** `GET /activities/week`  
**Summary:** Retrieves all journal entries for a 7-day period, grouped by day. Always returns an array of 7 days, even if some days have no entries.

### Request Parameters
- `weekStart` (required): The start date of the week in ISO format (e.g., `2026-04-07`).

### Response Body (`ApiResponse<WeekActivitiesResponse>`)
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "weekStart": "2026-04-07",
    "weekEnd": "2026-04-13",
    "totalEntries": 2,
    "storyGenerated": false,
    "days": [
      {
        "date": "2026-04-07",
        "dayOfWeek": "Tuesday",
        "entry": null
      },
      {
        "date": "2026-04-08",
        "dayOfWeek": "Wednesday",
        "entry": {
          "id": "d4e5f6a7-b8c9-d0e1-f2a3-b4c5d6e7f8a9",
          "content": "Went for a hike today.",
          "activityDate": "2026-04-08",
          "sttStatus": "NONE",
          "storyLocked": false,
          "createdAt": "2026-04-08T15:30:00",
          "updatedAt": "2026-04-08T15:30:00"
        }
      }
    ]
  },
  "timestamp": "2026-04-12T10:15:00"
}
```

### Business Logic
1. **Date Range Calculation**: Calculates the `weekEnd` date (start date + 6 days).
2. **Data Retrieval**: Fetches all active entries for the user within the calculated date range.
3. **Story Lock Check**: Determines if any entry within the week has `storyLocked = true`. If so, `storyGenerated` is flagged `true`.
4. **Data Aggregation**: Iterates through all 7 days of the requested week. Maps existing entries to their respective days or assigns `null` to days without entries.
5. **Response Mapping**: Returns the aggregated `WeekActivitiesResponse`.

---

## 4. Get Activity by ID

**Endpoint:** `GET /activities/{id}`  
**Summary:** Retrieves a specific journal entry by its UUID.

### Response Body (`ApiResponse<ActivityResponse>`)
*(Returns single ActivityResponse object)*

### Business Logic
1. **Data Retrieval**: Queries the database for the specific `activityId` ensuring it belongs to the `currentUser` and is not soft-deleted.
2. **Error Handling**: Throws `ResourceNotFoundException` (404) if the entry is missing, deleted, or belongs to another user.

---

## 5. Soft Delete Activity

**Endpoint:** `DELETE /activities/{id}`  
**Summary:** Soft deletes a journal entry, keeping it in the database for a 30-day recovery window.

### Response Body (`ApiResponse<Void>`)
```json
{
  "success": true,
  "message": "Activity deleted successfully",
  "data": null,
  "timestamp": "2026-04-12T10:20:00"
}
```

### Business Logic
1. **Data Retrieval**: Finds the active entry by ID and user. Throws `ResourceNotFoundException` (404) if not found.
2. **Lock Verification**: Checks the `storyLocked` flag. If `true`, throws an `ActivityLockedException` (423 Locked) preventing deletion of integrated story data.
3. **Soft Delete**: Sets the `deletedAt` timestamp to `LocalDateTime.now()` and saves the entity. This hides it from standard queries but preserves the data.

---

## 6. Get Deleted Activities

**Endpoint:** `GET /activities/deleted`  
**Summary:** Retrieves a list of soft-deleted activities that are within the 30-day recovery window.

### Response Body (`ApiResponse<List<ActivityResponse>>`)
*(Returns array of ActivityResponse objects)*

### Business Logic
1. **Window Calculation**: Calculates the cutoff date (current date minus 30 days).
2. **Data Retrieval**: Uses a custom JPQL query to fetch entries for the user where `deletedAt` is not null AND `deletedAt` is after the cutoff date, ordered by deletion time descending.
3. **Response Mapping**: Returns the list of recoverable entries.

---

## 7. Restore Activity

**Endpoint:** `PUT /activities/{id}/restore`  
**Summary:** Restores a soft-deleted journal entry, making it active again.

### Response Body (`ApiResponse<ActivityResponse>`)
*(Returns the restored ActivityResponse object)*

### Business Logic
1. **Data Retrieval**: Finds the entry by ID.
2. **Validation Checks**:
   - Ensures the entry belongs to the current user.
   - Ensures the entry is actually deleted (`deletedAt != null`).
   - Ensures the `deletedAt` timestamp is within the 30-day recovery window. Throws `ResourceNotFoundException` if the window has expired.
3. **Restoration**: Sets the `deletedAt` timestamp back to `null` and saves the entity.
4. **Response Mapping**: Returns the restored entity data.