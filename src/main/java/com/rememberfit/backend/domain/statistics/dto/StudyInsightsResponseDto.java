package com.rememberfit.backend.domain.statistics.dto;

import java.util.List;

public record StudyInsightsResponseDto(
        long totalStudyCount,
        int streakDays,
        List<DailyStudyCountDto> weeklyActivity,
        QualityDistributionDto qualityDistribution
) {
}
