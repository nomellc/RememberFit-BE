package com.rememberfit.backend.domain.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "refresh_tokens",
        indexes = @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 원문이 아니라 SHA-256 해시를 저장한다. DB가 유출돼도 토큰을 그대로 쓸 수 없다.
    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RefreshToken(Long userId, String tokenHash, Instant expiresAt, Instant createdAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
