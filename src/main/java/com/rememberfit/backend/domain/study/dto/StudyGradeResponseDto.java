package com.rememberfit.backend.domain.study.dto;

import com.rememberfit.backend.domain.card.entity.Card;

import java.time.LocalDate;

public record StudyGradeResponseDto(
        Long cardId,
        int repetition,
        int intervalDays,
        double easeFactor,
        LocalDate nextReviewDate
) {
    public StudyGradeResponseDto(Card card) {
        this(
                card.getId(),
                card.getRepetition(),
                card.getIntervalDays(),
                card.getEaseFactor(),
                card.getNextReviewDate()
        );
    }
}
