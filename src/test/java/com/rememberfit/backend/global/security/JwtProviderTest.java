package com.rememberfit.backend.global.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {
    private static final String SECRET = "test-only-secret-key-for-rememberfit-suite";
    private static final Instant NOW = Instant.parse("2026-10-23T00:00:00Z");

    private JwtProvider providerAt(Instant instant, String secret) {
        return new JwtProvider(
                new JwtProperties(secret, Duration.ofHours(1), Duration.ofDays(30)),
                Clock.fixed(instant, ZoneId.of("UTC"))
        );
    }

    @Test
    void returnsTheSubjectItSigned() {
        JwtProvider provider = providerAt(NOW, SECRET);

        String token = provider.createAccessToken(42L, "user@test.local");

        assertThat(provider.parse(token)).hasValueSatisfying(user -> {
            assertThat(user.id()).isEqualTo(42L);
            assertThat(user.email()).isEqualTo("user@test.local");
        });
    }

    @Test
    void rejectsATokenSignedWithAnotherKey() {
        String token = providerAt(NOW, SECRET).createAccessToken(42L, "user@test.local");
        JwtProvider otherServer = providerAt(NOW, "a-completely-different-secret-key-value");

        assertThat(otherServer.parse(token)).isEmpty();
    }

    @Test
    void rejectsATamperedToken() {
        String token = providerAt(NOW, SECRET).createAccessToken(42L, "user@test.local");
        // 서명 마지막 글자를 바꾼다. 서명 검증이 없다면 그대로 통과했을 것이다.
        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("A") ? "B" : "A");

        assertThat(providerAt(NOW, SECRET).parse(tampered)).isEmpty();
    }

    @Test
    void rejectsAnExpiredToken() {
        String token = providerAt(NOW, SECRET).createAccessToken(42L, "user@test.local");

        // 발급 1시간 뒤에는 더 이상 받아주지 않아야 한다.
        JwtProvider later = providerAt(NOW.plus(Duration.ofHours(1)).plusSeconds(1), SECRET);

        assertThat(later.parse(token)).isEmpty();
    }

    @Test
    void acceptsATokenThatHasNotExpiredYet() {
        String token = providerAt(NOW, SECRET).createAccessToken(42L, "user@test.local");

        JwtProvider slightlyLater = providerAt(NOW.plus(Duration.ofMinutes(59)), SECRET);

        assertThat(slightlyLater.parse(token)).isPresent();
    }

    @Test
    void rejectsGarbage() {
        JwtProvider provider = providerAt(NOW, SECRET);

        assertThat(provider.parse("not-a-token")).isEmpty();
        assertThat(provider.parse("")).isEmpty();
    }
}
