package com.rememberfit.backend.dto;

import com.rememberfit.backend.entity.Card;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardRequestDto {
    private String frontText;
    private String backText;
}
