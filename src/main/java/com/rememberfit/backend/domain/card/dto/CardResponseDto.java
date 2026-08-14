package com.rememberfit.backend.domain.card.dto;

import com.rememberfit.backend.domain.card.entity.Card;
import lombok.Getter;

@Getter
public class CardResponseDto {
    private Long id;
    private String frontText;
    private String backText;

    public CardResponseDto(Card card) {
        this.id = card.getId();
        this.frontText = card.getFrontText();
        this.backText = card.getBackText();
    }
}
