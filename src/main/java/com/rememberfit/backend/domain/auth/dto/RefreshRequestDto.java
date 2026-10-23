package com.rememberfit.backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequestDto {
    @NotBlank(message = "재발급 토큰이 필요해요.")
    private String refreshToken;
}
