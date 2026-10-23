package com.rememberfit.backend.domain.study.service;

import com.rememberfit.backend.domain.card.entity.Card;

public final class Sm2Calculator {
    // 복습 간격 상한. 상한이 없으면 성공 복습이 반복될 때 int 범위를 넘고,
    // 이어지는 studyDate.plusDays(interval) 에서 날짜 계산이 깨진다.
    private static final int MAX_INTERVAL_DAYS = 365;

    private Sm2Calculator() {
    }

    public static Sm2Result calculate(Card card, int quality) {
        int repetition = card.getRepetition();
        int interval = card.getIntervalDays();
        double easeFactor = card.getEaseFactor();

        if (quality >= 3) {
            if (repetition == 0) {
                interval = 1;
            } else if (repetition == 1) {
                interval = 6;
            } else {
                // 캐스팅 전에 상한으로 자른다. 순서가 바뀌면 오버플로가 먼저 일어난다.
                interval = (int) Math.min(Math.round(interval * easeFactor), MAX_INTERVAL_DAYS);
            }
            repetition++;
        } else {
            repetition = 0;
            interval = 1;
        }

        easeFactor += 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02);
        easeFactor = Math.max(1.3, easeFactor);

        return new Sm2Result(repetition, interval, easeFactor);
    }

    public record Sm2Result(int repetition, int interval, double easeFactor) {
    }
}
