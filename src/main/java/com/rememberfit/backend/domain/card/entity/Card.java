package com.rememberfit.backend.domain.card.entity;

import com.rememberfit.backend.domain.deck.entity.Deck;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long version;

    @Column(nullable = false, length = 200)
    private String frontText; // 앞면

    @Column(nullable = false, length = 1000)
    private String backText; // 뒷면

    private Integer repetition = 0; // 연속 정답 횟수
    private Integer intervalDays = 0; // 지난 복습 간격
    private Double easeFactor = 2.5; // 난이도 계수
    private LocalDate nextReviewDate;

    public void updateStudyStatus(int repetition, int intervalDays, double easeFactor, LocalDate nextDate) {
        this.repetition = repetition;
        this.intervalDays = intervalDays;
        this.easeFactor = easeFactor;
        this.nextReviewDate = nextDate;
    }

    public void updateContent(String frontText, String backText) {
        this.frontText = frontText;
        this.backText = backText;
    }

    // 카드(N) : 덱(1)
    @ManyToOne(fetch = FetchType.LAZY) // 여러 카드가 하나의 덱에 속한다
    @JoinColumn(name = "deck_id") // DB에는 deck_id라는 컬럼으로 foreign key 만들어라
    private Deck deck;

    public Card(String frontText, String backText, Deck deck) {
        this.frontText = frontText;
        this.backText = backText;
        this.deck = deck;
        deck.addCard(this);
    }
}
