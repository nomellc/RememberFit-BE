package com.rememberfit.backend.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {
    DECK_CREATED(HttpStatus.CREATED, "암기장을 만들었어요."),
    DECKS_READ(HttpStatus.OK, "암기장 목록을 불러왔어요."),
    DECK_UPDATED(HttpStatus.OK, "암기장 이름을 바꿨어요."),
    DECK_DELETED(HttpStatus.OK, "암기장을 삭제했어요."),
    CARD_CREATED(HttpStatus.CREATED, "카드를 만들었어요."),
    CARDS_READ(HttpStatus.OK, "카드 목록을 불러왔어요."),
    CARD_UPDATED(HttpStatus.OK, "카드를 수정했어요."),
    CARD_DELETED(HttpStatus.OK, "카드를 삭제했어요."),
    DUE_CARDS_READ(HttpStatus.OK, "오늘 학습할 카드를 불러왔어요."),
    STUDY_RECORDED(HttpStatus.OK, "학습 기록을 저장했어요."),
    STATISTICS_READ(HttpStatus.OK, "학습 통계를 불러왔어요."),
    INSIGHTS_READ(HttpStatus.OK, "학습 인사이트를 불러왔어요.");

    private final HttpStatus status;
    private final String message;
}
