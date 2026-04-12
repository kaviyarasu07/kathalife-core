# Authentication API Documentation

This document describes the endpoints and internal business logic of the Auth Module in the KathaLife Core application.

## Base URL
`/api/v1/auth` *(assuming `/api` context path configured in `application.yaml`)*

---

## 1. User Registration (Signup)

**Endpoint:** `POST /signup`  
**Summary:** Registers a new user in the system.

### Request Body (`SignupRequest`)
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```
*Note: Passwords must be at least 8 characters long and contain at least one uppercase letter, one number, and one special character. Language preference is set on the bio page later in the onboarding flow.*

### Response Body (`ApiResponse<AuthResponse>`)
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhb...",
    "refreshToken": "eyJhb...",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "bioCompleted": false,
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com"
  },
  "timestamp": "2023-10-01T12:00:00"
}
```

### Business Logic
1. **Validation**: 
   - Uses `@Email` for proper email structure validation.
   - Uses custom `@ValidPassword` for verifying that password contains at least 8 characters, one uppercase, one number, and one special character.
   - Checks if the provided email already exists. Throws `DuplicateEmailException` (409 Conflict) if true.
2. **User Creation**: 
   - Hashes the password using BCrypt.
   - Marks the user as active (`isActive = true`). (Note: `language_pref` is left null until updated on the bio profile page).
   - Saves the new user to the `users` table.
3. **Bio Profile Creation**: Creates and saves an empty `BioProfile` linked to the newly created user.
4. **Token Generation**: 
   - Generates a short-lived JWT Access Token (15 mins).
   - Generates a long-lived JWT Refresh Token (7 days).
5. **Token Storage**: 
   - Hashes the refresh token using SHA-256 and saves it to the `refresh_tokens` table with its expiration date.
6. **Response**: Returns the `AuthResponse` containing both tokens, user ID, email, and a `bioCompleted` flag set to `false`.

---

## 2. User Login

**Endpoint:** `POST /login`  
**Summary:** Authenticates a user and returns JWT tokens.

### Request Body (`LoginRequest`)
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```

### Response Body (`ApiResponse<AuthResponse>`)
*Same structure as the Signup Response. `bioCompleted` will be `true` or `false` depending on whether the user's full name is populated in their `BioProfile`.*

### Business Logic
1. **Authentication**: Uses Spring Security's `AuthenticationManager` to verify the email and password. Throws `BadCredentialsException` (401 Unauthorized) if invalid.
2. **User Retrieval**: Fetches the user entity from the database.
3. **Bio Completion Check**: Derives the `bioCompleted` boolean by checking if the user's `BioProfile` has a non-blank `fullName`.
4. **Old Tokens Cleanup**: Revokes all previous, non-revoked refresh tokens for this user in the database to prevent session bloat.
5. **Token Generation**: Generates new Access and Refresh tokens.
6. **Token Storage**: Hashes and saves the new refresh token to the database.
7. **Response**: Returns the `AuthResponse`.

---

## 3. Refresh Token

**Endpoint:** `POST /refresh`  
**Summary:** Issues a new access token using a valid refresh token.

### Request Body (`RefreshTokenRequest`)
```json
{
  "refreshToken": "eyJhb..."
}
```

### Response Body (`ApiResponse<AuthResponse>`)
*Same structure as the Login Response. A new `accessToken` is returned. The `refreshToken` remains the same as the one provided.*

### Business Logic
1. **Token Parsing**: Extracts the user's email from the provided refresh token's claims.
2. **User Retrieval**: Fetches the user from the database. Throws `ResourceNotFoundException` (404) if not found.
3. **Database Validation**: 
   - Hashes the provided refresh token and looks it up in the `refresh_tokens` table. 
   - Throws `InvalidTokenException` (401) if not found, if explicitly marked as revoked, or if its expiration date has passed.
4. **Token Generation**: Generates a new Access Token. The existing refresh token is reused and not rotated.
5. **Bio Completion Check**: Evaluates the `bioCompleted` flag for the user.
6. **Response**: Returns the new `AuthResponse`.

---

## 4. Forgot Password

**Endpoint:** `POST /forgot-password`  
**Summary:** Initiates the password reset flow by sending an OTP to the user's email. Supports idempotency; repeated calls within 15 minutes will reuse the exact same OTP without expiring it.

### Request Body (`ForgotPasswordRequest`)
```json
{
  "email": "user@example.com"
}
```

### Response Body (`ApiResponse<Void>`)
```json
{
  "success": true,
  "message": "If this email is registered, an OTP has been sent",
  "data": null,
  "timestamp": "2023-10-01T12:00:00"
}
```

### Business Logic
1. **User Registration Validation**: Silently checks if the provided email exists utilizing the `existsByEmail` repository method. If not registered, it logs the event and returns immediately (prevents email enumeration).
2. **Idempotency Check**: Checks if a valid, unexpired OTP already exists for this email with a status of `GENERATED`. 
   - **If yes:** Reuses the existing OTP, logs the event, and exits early. Multiple clicks within 15 minutes reuse the same OTP instead of creating new ones. This ensures the endpoint is idempotent.
3. **Cleanup Pending Requests**: If no valid OTP is found, it expires any old `GENERATED` requests for this email to `EXPIRED` ensuring older unused requests are invalidated.
4. **OTP Generation**: Generates a dynamic 6-digit random numeric OTP.
5. **Log Storage**: Hashes the OTP using SHA-256 and logs the request directly into the `password_change_requests` table with a status of `GENERATED` and a **15-minute** expiration time (`valid_to`).
6. **Email Sending**: (Placeholder) Logs the OTP to the console explicitly noting it is for development testing purposes only.

---

## 5. Reset Password

**Endpoint:** `POST /reset-password`  
**Summary:** Resets a user's password using a valid OTP.

### Request Body (`ResetPasswordRequest`)
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "newPassword": "newsecurepassword1",
  "confirmPassword": "newsecurepassword1"
}
```
*Note: Passwords must be at least 8 characters long and contain at least one uppercase letter, one number, and one special character.*

### Response Body (`ApiResponse<Void>`)
```json
{
  "success": true,
  "message": "Password reset successful",
  "data": null,
  "timestamp": "2023-10-01T12:00:00"
}
```

### Business Logic
1. **Password Validation**: Utilizes custom `@ValidPassword` to ensure structural validity.
2. **Password Match Validation**: Checks if `newPassword` and `confirmPassword` match. Throws `InvalidTokenException` (401) if they differ.
3. **User Lookup**: Retrieves the user by email. Throws `ResourceNotFoundException` (404) if not found.
4. **OTP Validation**: 
   - Hashes the provided OTP and queries the `password_change_requests` table for a record matching the `email`, `otp`, and `status = GENERATED`.
   - Throws `InvalidTokenException` (401) if no matching record is found.
   - If the current time is past the `valid_to` date (15 minutes after generation), the status is updated to `EXPIRED` in the database and an `InvalidTokenException` is thrown.
5. **Password Update**: Hashes the `newPassword` using BCrypt and updates the user's `passwordHash`.
6. **Token Invalidations**: 
   - Marks the change request status as `CHANGED` and saves it.
   - Deletes all refresh tokens for this user, forcing all devices to re-login with the new password.
7. **Response**: Returns a success message.

---

## Global Exception Handling

The module utilizes a `@RestControllerAdvice` (`GlobalExceptionHandler`) to map exceptions to standard HTTP status codes and `ApiResponse` envelopes seamlessly:
- **400 Bad Request**: Thrown for validation failures (e.g., missing fields, invalid email format, short passwords, missing caps/num). The response contains the specific validation error message.
- **401 Unauthorized**: Thrown for `BadCredentialsException` (invalid login) and `InvalidTokenException` (expired/invalid JWT or OTP).
- **404 Not Found**: Thrown for `ResourceNotFoundException` (e.g. attempting to reset an unregistered email).
- **409 Conflict**: Thrown for `DuplicateEmailException` during signup.
- **500 Internal Server Error**: Catch-all for unexpected server exceptions.
