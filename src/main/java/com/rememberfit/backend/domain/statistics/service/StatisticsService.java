package com.rememberfit.backend.domain.statistics.service;

import com.rememberfit.backend.domain.card.repository.CardRepository;
import com.rememberfit.backend.domain.statistics.dto.DailyStudyCountDto;
import com.rememberfit.backend.domain.statistics.dto.QualityDistributionDto;
import com.rememberfit.backend.domain.statistics.dto.StudyInsightsResponseDto;
import com.rememberfit.backend.domain.statistics.dto.StudyStatisticsResponseDto;
import com.rememberfit.backend.domain.study.repository.StudyLogRepository;
import com.rememberfit.backend.domain.study.repository.projection.DailyStudyCountProjection;
import com.rememberfit.backend.domain.study.repository.projection.QualityCountProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private static final int MASTERED_REPETITION_COUNT = 3;

    private final CardRepository cardRepository;
    private final StudyLogRepository studyLogRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public StudyStatisticsResponseDto getStudyStatistics() {
        LocalDate today = LocalDate.now(clock);
        long newCount = cardRepository.countByNextReviewDateIsNull();
        long reviewCount = cardRepository.countByNextReviewDateLessThanEqual(today);
        long doneCount = cardRepository.countByRepetitionGreaterThanEqual(MASTERED_REPETITION_COUNT);

        return new StudyStatisticsResponseDto(newCount, reviewCount, doneCount);
    }

    @Transactional(readOnly = true)
    public StudyInsightsResponseDto getStudyInsights() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = today.minusDays(6);

        List<DailyStudyCountDto> weeklyActivity = buildWeeklyActivity(startDate, today);
        QualityDistributionDto qualityDistribution = buildQualityDistribution();
        long totalStudyCount = qualityDistribution.againCount()
                + qualityDistribution.hardCount()
                + qualityDistribution.goodCount()
                + qualityDistribution.easyCount();
        int streakDays = calculateStreak(studyLogRepository.findDistinctStudyDatesDescending(), today);

        return new StudyInsightsResponseDto(
                totalStudyCount,
                streakDays,
                weeklyActivity,
                qualityDistribution
        );
    }

    private List<DailyStudyCountDto> buildWeeklyActivity(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Long> countsByDate = new HashMap<>();
        for (DailyStudyCountProjection projection
                : studyLogRepository.countDailyBetween(startDate, endDate)) {
            countsByDate.put(projection.getStudyDate(), projection.getCount());
        }

        List<DailyStudyCountDto> activity = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            activity.add(new DailyStudyCountDto(date, countsByDate.getOrDefault(date, 0L)));
        }
        return activity;
    }

    private QualityDistributionDto buildQualityDistribution() {
        long againCount = 0;
        long hardCount = 0;
        long goodCount = 0;
        long easyCount = 0;

        for (QualityCountProjection projection : studyLogRepository.countByQuality()) {
            switch (projection.getQuality()) {
                case 0, 1, 2 -> againCount += projection.getCount();
                case 3 -> hardCount += projection.getCount();
                case 4 -> goodCount += projection.getCount();
                case 5 -> easyCount += projection.getCount();
                default -> {
                    // DTO validation keeps new values in the 0..5 range.
                }
            }
        }
        return new QualityDistributionDto(againCount, hardCount, goodCount, easyCount);
    }

    private int calculateStreak(List<LocalDate> studyDates, LocalDate today) {
        if (studyDates.isEmpty()) {
            return 0;
        }

        LocalDate latestDate = studyDates.get(0);
        if (!latestDate.equals(today) && !latestDate.equals(today.minusDays(1))) {
            return 0;
        }

        int streak = 0;
        LocalDate expectedDate = latestDate;
        for (LocalDate studyDate : studyDates) {
            if (!studyDate.equals(expectedDate)) {
                break;
            }
            streak++;
            expectedDate = expectedDate.minusDays(1);
        }
        return streak;
    }
}
