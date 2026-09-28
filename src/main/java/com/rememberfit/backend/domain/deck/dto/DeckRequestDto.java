package com.rememberfit.backend.domain.deck.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeckRequestDto {
    @NotBlank(message = "암기장 이름을 입력해주세요.")
    @Size(max = 40, message = "암기장 이름은 40자 이하여야 해요.")
    private String title;
}
