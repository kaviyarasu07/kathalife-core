package com.kathalife.core.user.controller;

import com.kathalife.core.common.response.ApiResponse;
import com.kathalife.core.user.dto.*;
import com.kathalife.core.user.entity.User;
import com.kathalife.core.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User profile management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        UserResponse response = userService.getCurrentUser(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me/bio")
    @Operation(summary = "Get bio profile")
    public ResponseEntity<ApiResponse<BioProfileResponse>> getBio(@AuthenticationPrincipal User currentUser) {
        BioProfileResponse response = userService.getBioProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me/bio")
    @Operation(summary = "Update bio profile")
    public ResponseEntity<ApiResponse<BioProfileResponse>> updateBio(
            @AuthenticationPrincipal User currentUser,
            @RequestBody BioProfileRequest request) {

        BioProfileResponse response = userService.updateBioProfile(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Bio profile updated successfully"));
    }

    @GetMapping("/me/life-summary")
    @Operation(summary = "Get life summary")
    public ResponseEntity<ApiResponse<LifeSummaryResponse>> getSummary(@AuthenticationPrincipal User currentUser) {
        LifeSummaryResponse response = userService.getLifeSummary(currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me/life-summary")
    @Operation(summary = "Update life summary")
    public ResponseEntity<ApiResponse<LifeSummaryResponse>> updateSummary(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody LifeSummaryRequest request) {

        LifeSummaryResponse response = userService.updateLifeSummary(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Life summary updated successfully"));
    }
}
