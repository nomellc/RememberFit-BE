package com.rememberfit.backend.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 비밀 키는 기본값을 두지 않는다. 설정되지 않으면 애플리케이션이 시작되지 않는다.
 * 기본값을 두면 운영에 그대로 올라갈 수 있다.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {
    private static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret 설정이 필요합니다. JWT_SECRET 환경 변수를 지정해주세요.");
        }
        if (secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("app.jwt.secret 은 최소 " + MIN_SECRET_BYTES + "바이트여야 합니다.");
        }
        if (accessTokenTtl == null || accessTokenTtl.isZero() || accessTokenTtl.isNegative()) {
            throw new IllegalStateException("app.jwt.access-token-ttl 은 양수여야 합니다.");
        }
        if (refreshTokenTtl == null || refreshTokenTtl.isZero() || refreshTokenTtl.isNegative()) {
            throw new IllegalStateException("app.jwt.refresh-token-ttl 은 양수여야 합니다.");
        }
    }
}
