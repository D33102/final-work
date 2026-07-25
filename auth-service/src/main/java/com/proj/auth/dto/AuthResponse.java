package com.proj.auth.dto;

public record AuthResponse(
        String userId,
        String accessToken,
        String refreshToken
) {}
