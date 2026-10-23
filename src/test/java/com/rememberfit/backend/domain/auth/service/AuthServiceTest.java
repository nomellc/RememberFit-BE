package com.rememberfit.backend.domain.auth.service;

import com.rememberfit.backend.domain.auth.dto.AuthTokenResponseDto;
import com.rememberfit.backend.domain.auth.dto.LoginRequestDto;
import com.rememberfit.backend.domain.auth.dto.SignupRequestDto;
import com.rememberfit.backend.domain.auth.entity.RefreshToken;
import com.rememberfit.backend.domain.auth.repository.RefreshTokenRepository;
import com.rememberfit.backend.domain.user.entity.User;
import com.rememberfit.backend.domain.user.repository.UserRepository;
import com.rememberfit.backend.global.exception.CustomException;
import com.rememberfit.backend.global.exception.ErrorCode;
import com.rememberfit.backend.global.security.JwtProperties;
import com.rememberfit.backend.global.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-23T00:00:00Z");
    private static final Long USER_ID = 42L;

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtProvider jwtProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtProvider = mock(JwtProvider.class);
        JwtProperties properties = new JwtProperties(
                "test-only-secret-key-for-rememberfit-suite",
                Duration.ofHours(1),
                Duration.ofDays(30)
        );
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtProvider,
                properties,
                Clock.fixed(NOW, ZoneId.of("UTC"))
        );
    }

    @Test
    void signupStoresOnlyHashedPasswordAndIssuesTokens() {
        when(userRepository.existsByEmail("user@test.local")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> withId(invocation.getArgument(0)));
        when(jwtProvider.createAccessToken(USER_ID, "user@test.local")).thenReturn("access-token");

        AuthTokenResponseDto result = authService.signup(
                new SignupRequestDto("user@test.local", "plain-password", "테스터")
        );

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertThat(savedUser.getValue().getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getValue().getPasswordHash()).isNotEqualTo("plain-password");
        assertThat(savedUser.getValue().getCreatedAt())
                .isEqualTo(LocalDateTime.ofInstant(NOW, ZoneId.of("UTC")));

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.user().email()).isEqualTo("user@test.local");
        assertThat(result.user().nickname()).isEqualTo("테스터");
    }

    @Test
    void storesRefreshTokenAsHashNotAsRawValue() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> withId(invocation.getArgument(0)));

        AuthTokenResponseDto result = authService.signup(
                new SignupRequestDto("user@test.local", "plain-password", "테스터")
        );

        ArgumentCaptor<RefreshToken> saved = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(saved.capture());
        // DB 에 남는 값은 원문이 아니어야 한다.
        assertThat(saved.getValue().getTokenHash()).isNotEqualTo(result.refreshToken());
        assertThat(saved.getValue().getTokenHash()).hasSize(64);
        assertThat(saved.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(30)));
    }

    @Test
    void normalizesEmailBeforeCheckingAndSaving() {
        when(userRepository.existsByEmail("user@test.local")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> withId(invocation.getArgument(0)));

        authService.signup(new SignupRequestDto("  User@Test.Local  ", "plain-password", "테스터"));

        verify(userRepository).existsByEmail("user@test.local");
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("user@test.local");
    }

    @Test
    void rejectsSignupWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("user@test.local")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(
                new SignupRequestDto("user@test.local", "plain-password", "테스터")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginIssuesTokensWhenPasswordMatches() {
        User user = withId(new User("user@test.local", "hashed-password", "테스터", LocalDateTime.now()));
        when(userRepository.findByEmail("user@test.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "hashed-password")).thenReturn(true);
        when(jwtProvider.createAccessToken(USER_ID, "user@test.local")).thenReturn("access-token");

        AuthTokenResponseDto result = authService.login(
                new LoginRequestDto("user@test.local", "plain-password")
        );

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.user().id()).isEqualTo(USER_ID);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void rejectsLoginWhenPasswordDoesNotMatch() {
        User user = withId(new User("user@test.local", "hashed-password", "테스터", LocalDateTime.now()));
        when(userRepository.findByEmail("user@test.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequestDto("user@test.local", "wrong-password")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rejectsLoginForUnknownEmailWithTheSameErrorAndHashingCost() {
        when(userRepository.findByEmail("nobody@test.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new LoginRequestDto("nobody@test.local", "plain-password")))
                .isInstanceOf(CustomException.class)
                // 없는 계정과 틀린 비밀번호의 응답이 같아야 가입 여부가 드러나지 않는다.
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        // 응답 시간으로도 구분되지 않도록 같은 해싱 비용을 치러야 한다.
        verify(passwordEncoder).encode("plain-password");
    }

    @Test
    void refreshRotatesTokenAndIssuesNewPair() {
        String rawToken = "raw-refresh-token";
        RefreshToken stored = new RefreshToken(USER_ID, sha256Hex(rawToken), NOW.plusSeconds(60), NOW);
        User user = withId(new User("user@test.local", "hashed-password", "테스터", LocalDateTime.now()));
        when(refreshTokenRepository.findByTokenHash(sha256Hex(rawToken))).thenReturn(Optional.of(stored));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(USER_ID, "user@test.local")).thenReturn("new-access-token");

        AuthTokenResponseDto result = authService.refresh(rawToken);

        // 쓴 토큰은 폐기하고 새 토큰을 저장한다.
        verify(refreshTokenRepository).delete(stored);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isNotEqualTo(rawToken);
    }

    @Test
    void rejectsRefreshForUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown-token"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rejectsExpiredRefreshTokenAndStillDeletesIt() {
        String rawToken = "expired-token";
        RefreshToken expired = new RefreshToken(USER_ID, sha256Hex(rawToken), NOW, NOW.minusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(sha256Hex(rawToken))).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh(rawToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);

        // 만료된 토큰도 남겨두지 않는다.
        verify(refreshTokenRepository).delete(expired);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rejectsRefreshWhenOwnerNoLongerExists() {
        String rawToken = "orphan-token";
        RefreshToken stored = new RefreshToken(USER_ID, sha256Hex(rawToken), NOW.plusSeconds(60), NOW);
        when(refreshTokenRepository.findByTokenHash(sha256Hex(rawToken))).thenReturn(Optional.of(stored));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(rawToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
    }

    @Test
    void logoutRemovesEveryRefreshTokenOfTheUser() {
        authService.logout(USER_ID);

        verify(refreshTokenRepository).deleteByUserId(USER_ID);
    }

    @Test
    void getMeReturnsTheUserProfile() {
        User user = withId(new User("user@test.local", "hashed-password", "테스터", LocalDateTime.now()));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThat(authService.getMe(USER_ID).email()).isEqualTo("user@test.local");
        assertThat(authService.getMe(USER_ID).nickname()).isEqualTo("테스터");
    }

    @Test
    void getMeThrowsWhenUserIsMissing() {
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getMe(USER_ID))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    private static User withId(User user) {
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }

    private static String sha256Hex(String value) {
        try {
            return java.util.HexFormat.of().formatHex(
                    java.security.MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8))
            );
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
