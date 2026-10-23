package com.rememberfit.backend.domain.auth.controller;

import com.rememberfit.backend.domain.auth.dto.AuthTokenResponseDto;
import com.rememberfit.backend.domain.auth.dto.LoginRequestDto;
import com.rememberfit.backend.domain.auth.dto.RefreshRequestDto;
import com.rememberfit.backend.domain.auth.dto.SignupRequestDto;
import com.rememberfit.backend.domain.auth.dto.UserResponseDto;
import com.rememberfit.backend.domain.auth.service.AuthService;
import com.rememberfit.backend.global.response.ApiSuccess;
import com.rememberfit.backend.global.response.SuccessCode;
import com.rememberfit.backend.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @ApiSuccess(SuccessCode.SIGNED_UP)
    public AuthTokenResponseDto signup(@Valid @RequestBody SignupRequestDto requestDto) {
        return authService.signup(requestDto);
    }

    @PostMapping("/login")
    @ApiSuccess(SuccessCode.LOGGED_IN)
    public AuthTokenResponseDto login(@Valid @RequestBody LoginRequestDto requestDto) {
        return authService.login(requestDto);
    }

    @PostMapping("/refresh")
    @ApiSuccess(SuccessCode.TOKEN_REFRESHED)
    public AuthTokenResponseDto refresh(@Valid @RequestBody RefreshRequestDto requestDto) {
        return authService.refresh(requestDto.getRefreshToken());
    }

    @PostMapping("/logout")
    @ApiSuccess(SuccessCode.LOGGED_OUT)
    public void logout(@AuthenticationPrincipal AuthenticatedUser user) {
        authService.logout(user.id());
    }

    @GetMapping("/me")
    @ApiSuccess(SuccessCode.ME_READ)
    public UserResponseDto me(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.getMe(user.id());
    }
}
