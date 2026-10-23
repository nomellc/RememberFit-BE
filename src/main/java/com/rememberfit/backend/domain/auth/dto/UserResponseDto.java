package com.rememberfit.backend.domain.auth.dto;

import com.rememberfit.backend.domain.user.entity.User;

public record UserResponseDto(Long id, String email, String nickname) {
    public UserResponseDto(User user) {
        this(user.getId(), user.getEmail(), user.getNickname());
    }
}
