package com.rememberfit.backend.domain.study.service;

import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.card.repository.CardRepository;
import com.rememberfit.backend.domain.deck.entity.Deck;
import com.rememberfit.backend.domain.deck.repository.DeckRepository;
import com.rememberfit.backend.domain.study.dto.StudyGradeResponseDto;
import com.rememberfit.backend.domain.study.entity.StudyLog;
import com.rememberfit.backend.domain.study.repository.StudyLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudyServiceTest {

    @Test
    void gradesCardWithApplicationTimeZoneAndSavesStudyLog() {
        CardRepository cardRepository = mock(CardRepository.class);
        DeckRepository deckRepository = mock(DeckRepository.class);
        StudyLogRepository studyLogRepository = mock(StudyLogRepository.class);
        ZoneId seoul = ZoneId.of("Asia/Seoul");
        Clock clock = Clock.fixed(Instant.parse("2026-09-28T15:30:00Z"), seoul);
        StudyService studyService = new StudyService(
                cardRepository,
                deckRepository,
                studyLogRepository,
                clock
        );
        Card card = new Card("질문", "답", new Deck("테스트"));
        when(cardRepository.findByIdAndDeckId(10L, 20L)).thenReturn(Optional.of(card));

        StudyGradeResponseDto result = studyService.gradeCard(20L, 10L, 5);

        assertThat(result.nextReviewDate()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(result.intervalDays()).isEqualTo(1);

        ArgumentCaptor<StudyLog> logCaptor = ArgumentCaptor.forClass(StudyLog.class);
        verify(studyLogRepository).save(logCaptor.capture());
        StudyLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getDeckId()).isEqualTo(20L);
        assertThat(savedLog.getQuality()).isEqualTo(5);
        assertThat(savedLog.getStudyDate()).isEqualTo(LocalDate.of(2026, 9, 29));
        assertThat(savedLog.getNextReviewDate()).isEqualTo(LocalDate.of(2026, 9, 30));
    }
}
