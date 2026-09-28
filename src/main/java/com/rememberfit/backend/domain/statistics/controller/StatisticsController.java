package com.rememberfit.backend.domain.statistics.controller;

import com.rememberfit.backend.domain.statistics.dto.StudyStatisticsResponseDto;
import com.rememberfit.backend.domain.statistics.dto.StudyInsightsResponseDto;
import com.rememberfit.backend.domain.statistics.service.StatisticsService;
import com.rememberfit.backend.global.response.ApiSuccess;
import com.rememberfit.backend.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/summary")
    @ApiSuccess(SuccessCode.STATISTICS_READ)
    public StudyStatisticsResponseDto getSummary() {
        return statisticsService.getStudyStatistics();
    }

    @GetMapping("/insights")
    @ApiSuccess(SuccessCode.INSIGHTS_READ)
    public StudyInsightsResponseDto getInsights() {
        return statisticsService.getStudyInsights();
    }
}
