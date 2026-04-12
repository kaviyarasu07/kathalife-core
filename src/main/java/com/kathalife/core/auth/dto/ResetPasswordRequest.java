package com.kathalife.core.auth.dto;

import com.kathalife.core.common.validation.annotation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record ResetPasswordRequest(
    @NotBlank(message = "Email cannot be blank") 
    @Email(message = "Must be a valid email format") 
    String email,

    @NotBlank(message = "OTP cannot be blank") 
    String otp,

    @ValidPassword 
    String newPassword,

    @NotBlank(message = "Confirm password cannot be blank") 
    String confirmPassword
) {}
