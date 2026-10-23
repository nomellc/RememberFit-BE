package com.rememberfit.backend.global.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 설정이 잘못된 채로 애플리케이션이 뜨면, 운영에서 약한 키로 토큰을 발급하게 된다.
 * 시작 시점에 막는 것이 이 클래스의 목적이므로 그 동작을 고정해 둔다.
 */
class JwtPropertiesTest {
    private static final String VALID_SECRET = "test-only-secret-key-for-rememberfit-suite";

    @Test
    void acceptsAValidConfiguration() {
        assertThatCode(() -> new JwtProperties(VALID_SECRET, Duration.ofHours(1), Duration.ofDays(30)))
                .doesNotThrowAnyException();
    }

    @Test
    void refusesToStartWithoutASecret() {
        assertThatThrownBy(() -> new JwtProperties(null, Duration.ofHours(1), Duration.ofDays(30)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");

        assertThatThrownBy(() -> new JwtProperties("   ", Duration.ofHours(1), Duration.ofDays(30)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refusesASecretShorterThanTheSignatureRequires() {
        assertThatThrownBy(() -> new JwtProperties("too-short", Duration.ofHours(1), Duration.ofDays(30)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    @Test
    void refusesNonPositiveLifetimes() {
        assertThatThrownBy(() -> new JwtProperties(VALID_SECRET, null, Duration.ofDays(30)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProperties(VALID_SECRET, Duration.ZERO, Duration.ofDays(30)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProperties(VALID_SECRET, Duration.ofHours(-1), Duration.ofDays(30)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProperties(VALID_SECRET, Duration.ofHours(1), null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProperties(VALID_SECRET, Duration.ofHours(1), Duration.ZERO))
                .isInstanceOf(IllegalStateException.class);
    }
}
