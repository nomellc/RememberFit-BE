package com.rememberfit.backend.domain.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardRequestDto {
    @NotBlank(message = "카드 앞면을 입력해주세요.")
    @Size(max = 200, message = "카드 앞면은 200자 이하여야 해요.")
    private String frontText;

    @NotBlank(message = "카드 뒷면을 입력해주세요.")
    @Size(max = 1000, message = "카드 뒷면은 1,000자 이하여야 해요.")
    private String backText;
}
