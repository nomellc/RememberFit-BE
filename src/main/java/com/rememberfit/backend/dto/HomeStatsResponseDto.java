package com.rememberfit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HomeStatsResponseDto {
    private long newCount; // 새 카드 수
    private long reviewCount; // 복습할 카드 수
    private long doneCount; // 암기 완료 수
}
