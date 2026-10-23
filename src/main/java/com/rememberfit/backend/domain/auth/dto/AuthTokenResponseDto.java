package com.rememberfit.backend.domain.auth.dto;

public record AuthTokenResponseDto(
        String accessToken,
        String refreshToken,
        UserResponseDto user
) {
}
