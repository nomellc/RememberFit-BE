package com.rememberfit.backend.dto;

import com.rememberfit.backend.entity.Card;
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
