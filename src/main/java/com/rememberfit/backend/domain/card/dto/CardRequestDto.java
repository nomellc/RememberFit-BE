package com.rememberfit.backend.domain.card.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardRequestDto {
    private String frontText;
    private String backText;
}
