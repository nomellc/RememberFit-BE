package com.rememberfit.backend.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rememberfit.backend.global.exception.ErrorCode;
import com.rememberfit.backend.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/**
 * 보안 필터 단계의 실패도 컨트롤러 예외와 같은 본문 형식으로 내려준다.
 * 형식이 갈리면 클라이언트가 두 가지 오류 처리를 들고 있어야 한다.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorResponder {
    private final ObjectMapper objectMapper;

    public void write(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode)
            throws IOException {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                errorCode.getStatus().value(),
                errorCode.name(),
                errorCode.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
