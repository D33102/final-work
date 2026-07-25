package com.proj.auth.dto;


import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "User id is required")
        String userId,

        @NotBlank(message = "Password is required")
        String password
) {}
