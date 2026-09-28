package com.rememberfit.backend.domain.study.service;

import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.deck.entity.Deck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class Sm2CalculatorTest {
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 9, 28);

    @ParameterizedTest
    @CsvSource({
            "0, 0, 1, 1.70",
            "1, 0, 1, 1.96",
            "2, 0, 1, 2.18",
            "3, 1, 1, 2.36",
            "4, 1, 1, 2.50",
            "5, 1, 1, 2.60"
    })
    void calculatesFirstReviewForEachQuality(
            int quality,
            int expectedRepetition,
            int expectedInterval,
            double expectedEaseFactor
    ) {
        Card card = new Card("질문", "답", new Deck("테스트"));

        Sm2Calculator.Sm2Result result = Sm2Calculator.calculate(card, quality);

        assertThat(result.repetition()).isEqualTo(expectedRepetition);
        assertThat(result.interval()).isEqualTo(expectedInterval);
        assertThat(result.easeFactor()).isCloseTo(expectedEaseFactor, offset(0.0001));
    }

    @Test
    void increasesIntervalAcrossSuccessfulReviews() {
        Card card = new Card("질문", "답", new Deck("테스트"));

        apply(card, Sm2Calculator.calculate(card, 5));
        assertThat(card.getRepetition()).isEqualTo(1);
        assertThat(card.getIntervalDays()).isEqualTo(1);

        apply(card, Sm2Calculator.calculate(card, 5));
        assertThat(card.getRepetition()).isEqualTo(2);
        assertThat(card.getIntervalDays()).isEqualTo(6);

        apply(card, Sm2Calculator.calculate(card, 5));
        assertThat(card.getRepetition()).isEqualTo(3);
        assertThat(card.getIntervalDays()).isEqualTo(16);
        assertThat(card.getEaseFactor()).isCloseTo(2.8, offset(0.0001));
    }

    @Test
    void resetsRepetitionAfterFailedReviewAndKeepsEaseFactorFloor() {
        Card card = new Card("질문", "답", new Deck("테스트"));
        card.updateStudyStatus(4, 20, 1.4, BASE_DATE);

        Sm2Calculator.Sm2Result result = Sm2Calculator.calculate(card, 0);

        assertThat(result.repetition()).isZero();
        assertThat(result.interval()).isEqualTo(1);
        assertThat(result.easeFactor()).isEqualTo(1.3);
    }

    private void apply(Card card, Sm2Calculator.Sm2Result result) {
        card.updateStudyStatus(
                result.repetition(),
                result.interval(),
                result.easeFactor(),
                BASE_DATE.plusDays(result.interval())
        );
    }
}
