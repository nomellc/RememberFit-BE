package com.rememberfit.backend.domain.statistics.dto;

public record StudyStatisticsResponseDto(
        long newCount,
        long reviewCount,
        long doneCount
) {
}
