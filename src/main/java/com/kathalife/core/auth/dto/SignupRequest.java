package com.kathalife.core.auth.dto;

import com.kathalife.core.common.validation.annotation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    String email,

    @NotBlank(message = "Password is required")
    @ValidPassword
    String password
) {}
