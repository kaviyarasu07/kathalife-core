# KathaLife Core - Modular Monolith Package Structure

## Project Overview
A Spring Boot 3.5.1 modular monolith application with Java 21, PostgreSQL, JWT authentication, and Flyway migrations.

## Package Structure Summary

### 1. COMMON MODULE
**Responsibility**: Shared configurations, exceptions, utilities, and base entities

#### Configuration (com.kathalife.core.common.config)
- `AppConfig.java` - Base configuration with @EnableJpaAuditing
- `SecurityConfig.java` - Security configurations placeholder
- `OpenApiConfig.java` - OpenAPI/Swagger configurations placeholder

#### Exceptions (com.kathalife.core.common.exception)
- `GlobalExceptionHandler.java` - @RestControllerAdvice for centralized error handling
- `ResourceNotFoundException.java` - For missing resources
- `DuplicateEmailException.java` - For duplicate email registrations
- `InvalidTokenException.java` - For invalid JWT tokens

#### Response (com.kathalife.core.common.response)
- `ApiResponse<T>` - Generic API response record with success, message, data, timestamp
- `LanguageResponse.java` - DTO record for language details

#### Entity (com.kathalife.core.common.entity)
- `BaseEntity.java` - Abstract base class with:
  - UUID id (auto-generated)
  - LocalDateTime createdAt (@CreatedDate mapped to `created_at`, `updatable = false`)
  - LocalDateTime updatedAt (@LastModifiedDate mapped to `updated_at`)
  - @MappedSuperclass for inheritance
  - @EntityListeners(AuditingEntityListener.class)
- `Language.java` - @Entity mapping `languages` table

#### Repository (com.kathalife.core.common.repository)
- `LanguageRepository.java` - JpaRepository for languages

#### Service (com.kathalife.core.common.service)
- `LanguageService.java` - Interface
- `LanguageServiceImpl.java` - @Service implementation

#### Controller (com.kathalife.core.common.controller)
- `LanguageController.java` - GET `/v1/languages` (public endpoint)

#### Utilities & Validation (com.kathalife.core.common.validation)
- `ValidPassword.java` - Custom validation annotation for password strength (`@ValidPassword`)
- `PasswordValidator.java` - Implementation of the `ValidPassword` constraint using regex matching.
- `DateUtil.java` - Date/time utility methods (placeholder)

---

### 2. AUTH MODULE
**Responsibility**: Authentication, JWT token management, password reset flows

#### Controller (com.kathalife.core.auth.controller)
- `AuthController.java` - @RestController at /v1/auth

#### Service (com.kathalife.core.auth.service)
- `AuthService.java` - Interface for auth operations
- `AuthServiceImpl.java` - @Service implementation with @Slf4j
- `JwtService.java` - @Service for JWT token operations with @Slf4j

#### Repository (com.kathalife.core.auth.repository)
- `RefreshTokenRepository.java` - JpaRepository for RefreshToken
- `PasswordResetTokenRepository.java` - JpaRepository for PasswordResetToken
- `PasswordChangeRequestRepository.java` - JpaRepository for PasswordChangeRequest

#### Entity (com.kathalife.core.auth.entity)
- `RefreshToken.java` - @Entity (table: refresh_tokens)
  - Custom `createdAt` mapping and fields explicitly mapped to avoid Hibernate schema validation errors.
- `PasswordResetToken.java` - @Entity (table: password_reset_tokens)
  - Custom `createdAt` mapping to correctly map with database column.
- `PasswordChangeRequest.java` - @Entity (table: password_change_requests)
  - Logs password change requests utilizing a generated OTP. 

#### DTO (com.kathalife.core.auth.dto)
- `SignupRequest.java` - Record with @Email email, @ValidPassword password
- `LoginRequest.java` - Record with @Email email, password
- `AuthResponse.java` - Record with accessToken, refreshToken, tokenType, expiresIn
- `RefreshTokenRequest.java` - Record with refreshToken
- `ForgotPasswordRequest.java` - Record with @Email email
- `ResetPasswordRequest.java` - Record with @Email email, otp, @ValidPassword newPassword, confirmPassword

---

### 3. USER MODULE
**Responsibility**: User management, profiles, bio, and life summaries

#### Controller (com.kathalife.core.user.controller)
- `UserController.java` - @RestController at /v1/users

#### Service (com.kathalife.core.user.service)
- `UserService.java` - Interface for user operations
- `UserServiceImpl.java` - @Service implementation with @Slf4j

#### Repository (com.kathalife.core.user.repository)
- `UserRepository.java` - JpaRepository for User
- `BioProfileRepository.java` - JpaRepository for BioProfile
- `UserLifeSummaryRepository.java` - JpaRepository for UserLifeSummary

#### Entity (com.kathalife.core.user.entity)
- `User.java` - @Entity extends BaseEntity (table: users)
  - email, passwordHash, languagePref (nullable), isActive, gdprDeletionRequestedAt
- `BioProfile.java` - @Entity (table: bio_profiles)
  - user (OneToOne), fullName, dateOfBirth, hometown, occupation, familyNotes, profilePicUrl, updatedAt (mapped to `updated_at` directly)
- `UserLifeSummary.java` - @Entity (table: user_life_summaries)
  - user (OneToOne), summaryText, lastUpdatedAt (mapped to `last_updated_at` directly)

#### DTO (com.kathalife.core.user.dto)
- `UserResponse.java` - Record with id, email, languagePref, isActive, bioCompleted
- `BioProfileRequest.java` - Record with fullName, dateOfBirth, hometown, occupation, familyNotes, languagePref
- `BioProfileResponse.java` - Record with id, fullName, dateOfBirth, hometown, occupation, familyNotes, profilePicUrl, languagePref, updatedAt
- `LifeSummaryRequest.java` - Record with summaryText
- `LifeSummaryResponse.java` - Record with id, summaryText, lastUpdatedAt

---

### 4. JOURNAL MODULE
**Responsibility**: Journal activities and entries management

#### Controller (com.kathalife.core.journal.controller)
- `JournalController.java` - @RestController at /v1/journal

#### Service (com.kathalife.core.journal.service)
- `JournalService.java` - Interface for journal operations
- `JournalServiceImpl.java` - @Service implementation with @Slf4j

#### Repository (com.kathalife.core.journal.repository)
- `JournalActivityRepository.java` - JpaRepository for JournalActivity

#### Entity (com.kathalife.core.journal.entity)
- `JournalActivity.java` - @Entity extends BaseEntity (table: journal_activities)
  - user (ManyToOne), content, activityDate, audioFilePath, sttText, sttStatus (enum), deletedAt

#### DTO (com.kathalife.core.journal.dto)
- ✅ FIXED — `ActivityRequest.java` (content, activityDate)
- ✅ FIXED — `ActivityResponse.java` (id, content, activityDate, sttStatus, createdAt, updatedAt)
- ✅ FIXED — `ActivityUpdateRequest.java` (content)

---

## Architecture Principles Implemented

### ✅ Modular Monolith (NOT Microservices)
- Each module is self-contained with its own controller, service, repository, entity, and DTO layers
- Shared utilities in the common module
- Clear separation of concerns

### ✅ Communication Pattern
- Modules communicate through **service interfaces only**
- Repositories are never accessed directly between modules
- Service layer acts as boundary between modules

### ✅ Code Standards Applied
- **@Slf4j** on all service classes (Lombok)
- **Constructor injection only** - no @Autowired on fields
- **BaseEntity** for shared audit columns where applicable.
- **@EnableJpaAuditing** on AppConfig
- **Records** for all DTOs with @Valid annotations
- **@MappedSuperclass** for BaseEntity inheritance
- Each module has **package-info.java** describing responsibility
- **SOLID Validation Rules**: Custom `@ValidPassword` built for reusability. Jakarta `@Email` used for standardized validation rules at the DTO layer boundaries.

### ✅ Technology Stack
- Java 21
- Spring Boot 3.5.1
- Spring Data JPA with Hibernate
- PostgreSQL driver
- Flyway for migrations
- JWT (JJWT 0.12.6)
- Lombok
- Spring Validation
- SpringDoc OpenAPI (Swagger)

---

## File Count Summary
- **Total Java Classes**: 62
- **Configuration Classes**: 4 (Includes Auth filters)
- **Exception Classes**: 4
- **Entity Classes**: 8
- **Repository Interfaces**: 9
- **Service Interfaces**: 5
- **Service Implementations**: 7 (Includes UserDetailsService & JwtService)
- **Controller Classes**: 5
- **DTO Records**: 19
- **Utility/Validation Classes**: 3
- **Package Info Files**: 4

---

## Recent Updates
1. **Java Version Update**: Upgraded Java version from 17 to 21 in `pom.xml`.
2. **Schema Validation Fixes**: Updated JPA entities to match existing database schema exactly.
   - Refactored `BaseEntity` to explicitly map `@CreatedDate` to `created_at` (updatable=false) and `@LastModifiedDate` to `updated_at`.
   - `BioProfile` and `UserLifeSummary` modified to *not* inherit `BaseEntity` since they don't have a `created_at` column in the database schema.
   - Replaced inheritance with explicit mappings (`updatedAt` and `lastUpdatedAt` respectively) combined with `@EntityListeners(AuditingEntityListener.class)` to retain Spring Data JPA auto-population.
   - Updated `PasswordResetToken` and `RefreshToken` to not use `updated_at` as it's missing from their schema, keeping only custom `created_at` mapping.
3. **DTO Schema Alignment Fix**: Fixed 6 DTO records to match actual PostgreSQL database schema.
   - All field names now exactly match the database column mappings defined in JPA entities.
4. **Remaining DTO Fixes**:
   - ✅ LifeSummaryRequest.java — aligned with user_life_summaries table
   - ✅ ProfilePicResponse.java — aligned with bio_profiles table
5. **Auth Logic Implementation**: Fully implemented JWT authentication logic inside `AuthServiceImpl` alongside controllers, filters (`JwtAuthenticationFilter`), services (`JwtService`), and custom validation (`PasswordValidator`). 
6. **Solid DTO Validation**: Setup annotation-based declarative validation utilizing Jakarta `@Email` combined with customized constraint `@ValidPassword` for ensuring consistent input rules early across `SignupRequest` and `ResetPasswordRequest`. Auth API documented in `AUTH_API.md`.
7. **Forgot Password Refactoring**: Adjusted forgot password flow to explicitly check if an email exists (`existsByEmail`) returning a proper 404 response. Created a new table `password_change_requests` to meticulously log OTPs with `GENERATED`, `EXPIRED`, and `CHANGED` statuses. Integrated automatic expiration of earlier pending requests upon generating a new one.
8. **User Module Implementation**:
   - Implemented 5 User Profile APIs
   - GET /me — current user with bioCompleted flag
   - GET /me/bio — bio profile (empty object if not filled)
   - PUT /me/bio — partial update (null fields ignored)
   - GET /me/life-summary — life summary text
   - PUT /me/life-summary — upsert life summary
   - All APIs protected with JWT authentication
   - @AuthenticationPrincipal used for user extraction
   - Created USER_API.md documentation
9. **Languages Module Added**:
   - Created languages table with 6 Indian languages
   - Seed data: Tamil, Telugu, Hindi, Malayalam, Kannada, Bengali
   - Public API: GET /api/v1/languages
   - tts_supported and stt_supported flags per language
   - Used by bio page dropdown (no auth required)
10. **Language Preference Moved from Signup to Bio**:
    - Removed `languagePref` from `SignupRequest`
    - `language_pref` on users table now nullable
    - Added `languagePref` to `BioProfileRequest`
    - Added `languagePref` to `BioProfileResponse`
    - `UserServiceImpl` updates `users.language_pref` when bio profile is saved
    - Cleaner signup flow — fewer fields at registration

## Next Steps
1. Implement business logic in Journal service methods
2. Write unit and integration tests
