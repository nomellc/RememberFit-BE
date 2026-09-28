package com.rememberfit.backend.domain.statistics;

import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.card.repository.CardRepository;
import com.rememberfit.backend.domain.deck.entity.Deck;
import com.rememberfit.backend.domain.deck.repository.DeckRepository;
import com.rememberfit.backend.domain.statistics.dto.StudyStatisticsResponseDto;
import com.rememberfit.backend.domain.statistics.dto.StudyInsightsResponseDto;
import com.rememberfit.backend.domain.statistics.service.StatisticsService;
import com.rememberfit.backend.domain.study.entity.StudyLog;
import com.rememberfit.backend.domain.study.repository.StudyLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private Clock clock;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void separatesNeverStudiedCardsFromScheduledReviews() {
        Deck deck = deckRepository.save(new Deck("통계 테스트"));
        LocalDate today = LocalDate.now(clock);

        Card newCard = new Card("새 카드", "아직 학습 전", deck);

        Card failedButScheduled = new Card("다시 학습", "신규로 되돌아가면 안 됨", deck);
        failedButScheduled.updateStudyStatus(0, 1, 2.3, today.plusDays(1));

        Card dueCard = new Card("오늘 복습", "복습 예정", deck);
        dueCard.updateStudyStatus(1, 1, 2.5, today);

        Card masteredAndDue = new Card("기억 완료", "누적 상태이므로 복습과 중복 가능", deck);
        masteredAndDue.updateStudyStatus(3, 10, 2.7, today);

        cardRepository.save(newCard);
        cardRepository.save(failedButScheduled);
        cardRepository.save(dueCard);
        cardRepository.save(masteredAndDue);

        StudyStatisticsResponseDto statistics = statisticsService.getStudyStatistics();

        assertThat(statistics.newCount()).isEqualTo(1);
        assertThat(statistics.reviewCount()).isEqualTo(2);
        assertThat(statistics.doneCount()).isEqualTo(1);
    }

    @Test
    void exposesStatisticsThroughDomainBasedApiPathAndCommonResponse() throws Exception {
        mockMvc.perform(get("/api/statistics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("학습 통계를 불러왔어요."))
                .andExpect(jsonPath("$.data.newCount").isNumber())
                .andExpect(jsonPath("$.data.reviewCount").isNumber())
                .andExpect(jsonPath("$.data.doneCount").isNumber());

        mockMvc.perform(get("/api/home/stats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("API_PATH_NOT_FOUND"));
    }

    @Test
    void calculatesWeeklyActivityQualityDistributionAndCurrentStreak() {
        LocalDate today = LocalDate.now(clock);
        Instant now = clock.instant();
        studyLogRepository.saveAll(List.of(
                studyLog(1L, 1, today, now),
                studyLog(2L, 2, today, now),
                studyLog(3L, 3, today.minusDays(1), now.minusSeconds(86_400)),
                studyLog(4L, 4, today.minusDays(2), now.minusSeconds(86_400 * 2)),
                studyLog(5L, 5, today.minusDays(4), now.minusSeconds(86_400 * 4))
        ));

        StudyInsightsResponseDto insights = statisticsService.getStudyInsights();

        assertThat(insights.totalStudyCount()).isEqualTo(5);
        assertThat(insights.streakDays()).isEqualTo(3);
        assertThat(insights.weeklyActivity()).hasSize(7);
        assertThat(insights.weeklyActivity().get(6).count()).isEqualTo(2);
        assertThat(insights.qualityDistribution().againCount()).isEqualTo(2);
        assertThat(insights.qualityDistribution().hardCount()).isEqualTo(1);
        assertThat(insights.qualityDistribution().goodCount()).isEqualTo(1);
        assertThat(insights.qualityDistribution().easyCount()).isEqualTo(1);
    }

    private StudyLog studyLog(Long cardId, int quality, LocalDate studyDate, Instant studiedAt) {
        return new StudyLog(
                cardId,
                1L,
                quality,
                quality >= 3 ? 1 : 0,
                1,
                2.5,
                studyDate.plusDays(1),
                studyDate,
                studiedAt
        );
    }
}
