# User Profile API Documentation

This document describes the endpoints and internal business logic of the User Module in the KathaLife Core application. All endpoints require a valid JWT Bearer token in the `Authorization` header.

## Base URL
`/api/v1/users`

---

## 1. Get Current User Profile

**Endpoint:** `GET /me`  
**Summary:** Retrieves the profile of the currently authenticated user.

### Request
- **Headers**: `Authorization: Bearer <your_jwt_token>`

### Response Body (`ApiResponse<UserResponse>`)
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "languagePref": "ta",
    "isActive": true,
    "bioCompleted": true
  },
  "timestamp": "2023-10-01T12:00:00"
}
```

### Business Logic
1. **Authentication**: The request is authenticated via the `JwtAuthenticationFilter`.
2. **User Extraction**: The full `User` entity is injected into the controller method via the `@AuthenticationPrincipal` annotation.
3. **Bio Completion Check**: The service checks if the user's `BioProfile` exists and has a non-blank `fullName` to determine the `bioCompleted` flag.
4. **Response Mapping**: The service maps the `User` entity and the `bioCompleted` flag to a `UserResponse` DTO.

---

## 2. Get Bio Profile

**Endpoint:** `GET /me/bio`  
**Summary:** Retrieves the biographical profile of the currently authenticated user.

### Request
- **Headers**: `Authorization: Bearer <your_jwt_token>`

### Response Body (`ApiResponse<BioProfileResponse>`)
*If the bio is not filled, all fields in the `data` object will be `null`.*
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
    "fullName": "John Doe",
    "dateOfBirth": "1990-01-15",
    "hometown": "New York",
    "occupation": "Software Engineer",
    "familyNotes": "Married with two children.",
    "profilePicUrl": null,
    "languagePref": "ta",
    "updatedAt": "2023-10-01T11:30:00"
  },
  "timestamp": "2023-10-01T12:01:00"
}
```

### Business Logic
1. **Authentication & User Extraction**: Same as above.
2. **Data Retrieval**: The service attempts to find the `BioProfile` associated with the current user.
3. **Response Mapping**: 
   - If a `BioProfile` is found, it is mapped to a `BioProfileResponse` DTO. `languagePref` is mapped dynamically by querying the current user's attached entity state.
   - If not found, a `BioProfileResponse` with all `null` fields is returned, ensuring a consistent response structure.

---

## 3. Update Bio Profile

**Endpoint:** `PUT /me/bio`  
**Summary:** Updates the biographical profile of the currently authenticated user. This is a partial update; only non-null fields in the request will be updated.

### Request Body (`BioProfileRequest`)
*Send only the fields you want to update.*
```json
{
  "fullName": "Johnathan Doe",
  "hometown": "San Francisco",
  "languagePref": "hi"
}
```

### Response Body (`ApiResponse<BioProfileResponse>`)
*Returns the complete, updated bio profile along with the updated global language preference.*
```json
{
  "success": true,
  "message": "Bio profile updated successfully",
  "data": {
    "id": "a1b2c3d4-e5f6-a7b8-c9d0-e1f2a3b4c5d6",
    "fullName": "Johnathan Doe",
    "dateOfBirth": "1990-01-15",
    "hometown": "San Francisco",
    "occupation": "Software Engineer",
    "familyNotes": "Married with two children.",
    "profilePicUrl": null,
    "languagePref": "hi",
    "updatedAt": "2023-10-01T12:05:00"
  },
  "timestamp": "2023-10-01T12:05:00"
}
```

### Business Logic
1. **Authentication & User Extraction**: Same as above.
2. **Data Retrieval (Upsert)**: The service finds the user's existing `BioProfile`. If one doesn't exist (e.g., legacy user), it creates a new one linked to the current user.
3. **Partial Update**: The service iterates through the fields of the `BioProfileRequest`. For each non-null field, it updates the corresponding field on the `BioProfile` entity.
4. **User Preference Update**: If `languagePref` is not null or blank inside the request, it is updated and securely saved against the parent `User` entity automatically handling language choice mapping.
5. **Persistence**: The updated `BioProfile` is saved to the database. The `updatedAt` timestamp is automatically handled by `@LastModifiedDate`.
6. **Response Mapping**: The saved entity is mapped to a `BioProfileResponse` and returned alongside the newly updated language preference.

---

## 4. Get Life Summary

**Endpoint:** `GET /me/life-summary`  
**Summary:** Retrieves the life summary of the currently authenticated user.

### Request
- **Headers**: `Authorization: Bearer <your_jwt_token>`

### Response Body (`ApiResponse<LifeSummaryResponse>`)
*If the summary is not filled, all fields in the `data` object will be `null`.*
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "b2c3d4e5-f6a7-b8c9-d0e1-f2a3b4c5d6e7",
    "summaryText": "A brief summary of my life's journey...",
    "lastUpdatedAt": "2023-09-30T18:00:00"
  },
  "timestamp": "2023-10-01T12:10:00"
}
```

### Business Logic
1. **Authentication & User Extraction**: Same as above.
2. **Data Retrieval**: The service attempts to find the `UserLifeSummary` associated with the current user.
3. **Response Mapping**: 
   - If a `UserLifeSummary` is found, it is mapped to a `LifeSummaryResponse` DTO.
   - If not found, a `LifeSummaryResponse` with all `null` fields is returned.

---

## 5. Update Life Summary

**Endpoint:** `PUT /me/life-summary`  
**Summary:** Creates or updates the life summary for the currently authenticated user.

### Request Body (`LifeSummaryRequest`)
```json
{
  "summaryText": "An updated summary of my life's journey and future goals."
}
```

### Response Body (`ApiResponse<LifeSummaryResponse>`)
*Returns the complete, updated life summary.*
```json
{
  "success": true,
  "message": "Life summary updated successfully",
  "data": {
    "id": "b2c3d4e5-f6a7-b8c9-d0e1-f2a3b4c5d6e7",
    "summaryText": "An updated summary of my life's journey and future goals.",
    "lastUpdatedAt": "2023-10-01T12:15:00"
  },
  "timestamp": "2023-10-01T12:15:00"
}
```

### Business Logic
1. **Authentication & User Extraction**: Same as above.
2. **Validation**: The `@Valid` annotation ensures the `summaryText` field is not blank.
3. **Data Retrieval (Upsert)**: The service finds the user's existing `UserLifeSummary`. If one doesn't exist, it creates a new one linked to the current user.
4. **Update**: The `summaryText` from the request is set on the entity.
5. **Persistence**: The `UserLifeSummary` is saved. The `lastUpdatedAt` timestamp is automatically handled by `@LastModifiedDate`.
6. **Response Mapping**: The saved entity is mapped to a `LifeSummaryResponse` and returned.
