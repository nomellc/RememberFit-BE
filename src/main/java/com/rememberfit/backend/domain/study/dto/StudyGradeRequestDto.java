package com.rememberfit.backend.domain.study.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StudyGradeRequestDto {
    @Min(value = 0, message = "학습 점수는 0 이상이어야 해요.")
    @Max(value = 5, message = "학습 점수는 5 이하여야 해요.")
    private int quality;
}
