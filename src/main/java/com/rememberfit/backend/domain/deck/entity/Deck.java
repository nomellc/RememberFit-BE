package com.rememberfit.backend.domain.deck.entity;

import com.rememberfit.backend.domain.card.entity.Card;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "decks", indexes = @Index(name = "idx_decks_user_id", columnList = "user_id"))
public class Deck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소유자. 모든 조회는 이 값으로 걸러진다.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 40)
    private String title;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 덱(1) : 카드(N)
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    public Deck(String title, Long userId) {
        this.title = title;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void removeCard(Card card) {
        cards.remove(card);
    }
}
