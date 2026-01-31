package com.rememberfit.backend.utils;

import com.rememberfit.backend.entity.Card;

public class SM2Utils {
    // 계산 결과를 담아서 돌려줄 박스
    public static class SM2Result {
        public int repetition;
        public int interval;
        public double easeFactor;

        public SM2Result(int repetition, int interval, double easeFactor) {
            this.repetition = repetition;
            this.interval = interval;
            this.easeFactor = easeFactor;
        }
    }

    public static SM2Result calculate(Card card, int quality) {
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

        easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        if (easeFactor < 1.3) {
            easeFactor = 1.3;
        }
        return new SM2Result(repetition, interval, easeFactor);
    }
}
