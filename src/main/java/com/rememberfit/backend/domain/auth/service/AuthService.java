package com.rememberfit.backend.domain.auth.service;

import com.rememberfit.backend.domain.auth.dto.AuthTokenResponseDto;
import com.rememberfit.backend.domain.auth.dto.LoginRequestDto;
import com.rememberfit.backend.domain.auth.dto.SignupRequestDto;
import com.rememberfit.backend.domain.auth.dto.UserResponseDto;
import com.rememberfit.backend.domain.auth.entity.RefreshToken;
import com.rememberfit.backend.domain.auth.repository.RefreshTokenRepository;
import com.rememberfit.backend.domain.user.entity.User;
import com.rememberfit.backend.domain.user.repository.UserRepository;
import com.rememberfit.backend.global.exception.CustomException;
import com.rememberfit.backend.global.exception.ErrorCode;
import com.rememberfit.backend.global.security.JwtProperties;
import com.rememberfit.backend.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthTokenResponseDto signup(SignupRequestDto request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = userRepository.save(new User(
                email,
                passwordEncoder.encode(request.getPassword()),
                request.getNickname().trim(),
                LocalDateTime.now(clock)
        ));
        return issueTokens(user);
    }

    @Transactional
    public AuthTokenResponseDto login(LoginRequestDto request) {
        Optional<User> found = userRepository.findByEmail(normalizeEmail(request.getEmail()));

        if (found.isEmpty()) {
            // 가입되지 않은 이메일에도 같은 해싱 비용을 치른다.
            // 그러지 않으면 응답 시간 차이로 가입 여부를 알아낼 수 있다.
            passwordEncoder.encode(request.getPassword());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = found.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // 비밀번호가 틀린 경우와 없는 계정의 응답을 구분하지 않는다.
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthTokenResponseDto refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hashToken(refreshToken))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        // 한 번 쓴 재발급 토큰은 만료 여부와 관계없이 폐기한다(회전).
        // 탈취된 토큰이 반복해서 쓰이는 것을 막는다.
        refreshTokenRepository.delete(stored);

        if (stored.isExpired(clock.instant())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
        return issueTokens(user);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getMe(Long userId) {
        return userRepository.findById(userId)
                .map(UserResponseDto::new)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private AuthTokenResponseDto issueTokens(User user) {
        Instant now = clock.instant();
        String refreshToken = generateRefreshToken();

        refreshTokenRepository.save(new RefreshToken(
                user.getId(),
                hashToken(refreshToken),
                now.plus(jwtProperties.refreshTokenTtl()),
                now
        ));

        return new AuthTokenResponseDto(
                jwtProvider.createAccessToken(user.getId(), user.getEmail()),
                refreshToken,
                new UserResponseDto(user)
        );
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 재발급 토큰은 원문을 저장하지 않는다. 조회도 해시로 한다. */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 을 사용할 수 없습니다.", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
