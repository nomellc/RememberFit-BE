package com.rememberfit.backend.domain.study.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "study_logs",
        indexes = {
                @Index(name = "idx_study_logs_study_date", columnList = "study_date"),
                @Index(name = "idx_study_logs_card_id", columnList = "card_id")
        }
)
public class StudyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "deck_id", nullable = false)
    private Long deckId;

    @Column(nullable = false)
    private int quality;

    @Column(name = "repetition_after", nullable = false)
    private int repetitionAfter;

    @Column(name = "interval_days_after", nullable = false)
    private int intervalDaysAfter;

    @Column(name = "ease_factor_after", nullable = false)
    private double easeFactorAfter;

    @Column(name = "next_review_date", nullable = false)
    private LocalDate nextReviewDate;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "studied_at", nullable = false)
    private Instant studiedAt;

    public StudyLog(
            Long cardId,
            Long deckId,
            int quality,
            int repetitionAfter,
            int intervalDaysAfter,
            double easeFactorAfter,
            LocalDate nextReviewDate,
            LocalDate studyDate,
            Instant studiedAt
    ) {
        this.cardId = cardId;
        this.deckId = deckId;
        this.quality = quality;
        this.repetitionAfter = repetitionAfter;
        this.intervalDaysAfter = intervalDaysAfter;
        this.easeFactorAfter = easeFactorAfter;
        this.nextReviewDate = nextReviewDate;
        this.studyDate = studyDate;
        this.studiedAt = studiedAt;
    }
}
