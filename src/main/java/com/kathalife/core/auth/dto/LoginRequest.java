package com.kathalife.core.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email cannot be blank") 
    @Email(message = "Must be a valid email format") 
    String email,

    @NotBlank(message = "Password cannot be blank") 
    String password
) {}
