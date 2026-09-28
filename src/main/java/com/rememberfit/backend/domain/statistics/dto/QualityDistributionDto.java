package com.rememberfit.backend.domain.statistics.dto;

public record QualityDistributionDto(
        long againCount,
        long hardCount,
        long goodCount,
        long easyCount
) {
}
