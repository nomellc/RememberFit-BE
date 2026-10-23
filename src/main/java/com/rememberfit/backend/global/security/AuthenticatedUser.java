package com.rememberfit.backend.global.security;

/** 인증된 요청의 주체. 컨트롤러는 이 값으로만 사용자를 식별한다. */
public record AuthenticatedUser(Long id, String email) {
}
