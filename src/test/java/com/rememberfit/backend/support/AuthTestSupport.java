package com.rememberfit.backend.support;

import com.rememberfit.backend.domain.user.entity.User;
import com.rememberfit.backend.domain.user.repository.UserRepository;
import com.rememberfit.backend.global.security.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * 통합 테스트에서 인증된 사용자를 만든다.
 * 토큰 발급·검증 자체는 AuthApiIntegrationTest 가 실제 Bearer 토큰으로 따로 검증한다.
 * 여기서는 그 뒤 단계(소유권 기반 조회)에 집중하기 위해 주체만 주입한다.
 */
public final class AuthTestSupport {

    private AuthTestSupport() {
    }

    public static User createUser(UserRepository userRepository, String email) {
        return userRepository.saveAndFlush(
                new User(email, "(test)", "테스터", LocalDateTime.of(2026, 1, 1, 0, 0))
        );
    }

    public static RequestPostProcessor asUser(User user) {
        return authentication(new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(user.getId(), user.getEmail()),
                null,
                List.of()
        ));
    }
}
