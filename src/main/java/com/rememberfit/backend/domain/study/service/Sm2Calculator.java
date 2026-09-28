package com.rememberfit.backend.domain.study.service;

import com.rememberfit.backend.domain.card.entity.Card;

public final class Sm2Calculator {

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
                interval = (int) Math.round(interval * easeFactor);
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
