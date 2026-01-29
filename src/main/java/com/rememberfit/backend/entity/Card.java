package com.rememberfit.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String frontText; // 앞면

    @Column(nullable = false)
    private String backText; // 뒷면

    // 카드(N) : 덱(1)
    @ManyToOne(fetch = FetchType.LAZY) // 여러 카드가 하나의 덱에 속한다
    @JoinColumn(name = "deck_id") // DB에는 deck_id라는 컬럼으로 foreign key 만들어라
    private Deck deck;

    public Card(String frontText, String backText, Deck deck) {
        this.frontText = frontText;
        this.backText = backText;
        this.deck = deck;
    }
}
