package com.rememberfit.backend.domain.study.service;

import com.rememberfit.backend.domain.card.dto.CardResponseDto;
import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.card.repository.CardRepository;
import com.rememberfit.backend.domain.deck.entity.Deck;
import com.rememberfit.backend.domain.deck.repository.DeckRepository;
import com.rememberfit.backend.domain.study.dto.StudyGradeResponseDto;
import com.rememberfit.backend.domain.study.entity.StudyLog;
import com.rememberfit.backend.domain.study.repository.StudyLogRepository;
import com.rememberfit.backend.global.exception.CustomException;
import com.rememberfit.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StudyServiceTest {
    // 2026-09-28T15:30:00Z 는 Asia/Seoul 기준 2026-09-29T00:30 이다.
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-09-28T15:30:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    private CardRepository cardRepository;
    private DeckRepository deckRepository;
    private StudyLogRepository studyLogRepository;
    private StudyService studyService;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        deckRepository = mock(DeckRepository.class);
        studyLogRepository = mock(StudyLogRepository.class);
        studyService = new StudyService(cardRepository, deckRepository, studyLogRepository, CLOCK);
    }

    @Test
    void gradesCardWithApplicationTimeZoneAndSavesStudyLog() {
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

    @Test
    void returnsDueCardsQueriedWithApplicationTimeZoneDate() {
        Deck deck = new Deck("테스트");
        when(deckRepository.existsById(20L)).thenReturn(true);
        when(cardRepository.findDueCards(20L, LocalDate.of(2026, 9, 29)))
                .thenReturn(List.of(new Card("앞면1", "뒷면1", deck), new Card("앞면2", "뒷면2", deck)));

        List<CardResponseDto> result = studyService.getDueCards(20L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFrontText()).isEqualTo("앞면1");
        assertThat(result.get(0).getBackText()).isEqualTo("뒷면1");
        assertThat(result.get(1).getFrontText()).isEqualTo("앞면2");
        verify(cardRepository).findDueCards(20L, LocalDate.of(2026, 9, 29));
    }

    @Test
    void throwsDeckNotFoundBeforeQueryingCardsWhenDeckIsMissing() {
        when(deckRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studyService.getDueCards(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DECK_NOT_FOUND);

        verifyNoInteractions(cardRepository);
    }

    @Test
    void returnsEmptyListWhenDeckHasNoDueCards() {
        when(deckRepository.existsById(20L)).thenReturn(true);
        when(cardRepository.findDueCards(20L, LocalDate.of(2026, 9, 29))).thenReturn(List.of());

        assertThat(studyService.getDueCards(20L)).isEmpty();
    }

    @Test
    void throwsDeckNotFoundWhenGradingCardInMissingDeck() {
        when(cardRepository.findByIdAndDeckId(10L, 99L)).thenReturn(Optional.empty());
        when(deckRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studyService.gradeCard(99L, 10L, 5))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DECK_NOT_FOUND);

        verify(studyLogRepository, never()).save(any());
    }

    @Test
    void throwsCardNotFoundWhenDeckExistsButCardDoesNot() {
        when(cardRepository.findByIdAndDeckId(10L, 20L)).thenReturn(Optional.empty());
        when(deckRepository.existsById(20L)).thenReturn(true);

        assertThatThrownBy(() -> studyService.gradeCard(20L, 10L, 5))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CARD_NOT_FOUND);

        verify(studyLogRepository, never()).save(any());
    }

    @Test
    void doesNotQueryDeckWhenGradingSucceeds() {
        Card card = new Card("질문", "답", new Deck("테스트"));
        when(cardRepository.findByIdAndDeckId(10L, 20L)).thenReturn(Optional.of(card));

        studyService.gradeCard(20L, 10L, 5);

        verify(deckRepository, never()).existsById(any());
    }
}
