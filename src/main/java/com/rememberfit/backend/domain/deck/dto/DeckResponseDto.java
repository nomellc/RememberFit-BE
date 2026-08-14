package com.rememberfit.backend.domain.deck.dto;

import com.rememberfit.backend.domain.deck.entity.Deck;
import lombok.Getter;

@Getter
public class DeckResponseDto {
    private Long id;
    private String title;
    private int cardCount;

    // Entity -> DTO 변환

    public DeckResponseDto(Deck deck) {
        this.id = deck.getId();
        this.title = deck.getTitle();
        this.cardCount = deck.getCards().size();
    }
}
