package com.rememberfit.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값을 확인해주세요."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없어요."),
    INVALID_PATH_PARAMETER(HttpStatus.BAD_REQUEST, "주소의 식별자 형식이 올바르지 않아요."),
    DECK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 암기장을 찾을 수 없어요."),
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카드를 찾을 수 없어요."),
    API_PATH_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 API를 찾을 수 없어요."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식이에요."),
    DATA_CONFLICT(HttpStatus.CONFLICT, "현재 데이터 상태에서는 요청을 처리할 수 없어요."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 문제가 발생했어요. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String message;
}
