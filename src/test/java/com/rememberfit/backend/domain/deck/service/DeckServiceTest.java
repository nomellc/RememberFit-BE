package com.rememberfit.backend.domain.deck.service;

import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.deck.dto.DeckRequestDto;
import com.rememberfit.backend.domain.deck.dto.DeckResponseDto;
import com.rememberfit.backend.domain.deck.entity.Deck;
import com.rememberfit.backend.domain.deck.repository.DeckRepository;
import com.rememberfit.backend.global.exception.CustomException;
import com.rememberfit.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeckServiceTest {
    private static final Long USER_ID = 7L;

    private DeckRepository deckRepository;
    private DeckService deckService;

    @BeforeEach
    void setUp() {
        deckRepository = mock(DeckRepository.class);
        deckService = new DeckService(deckRepository);
    }

    @Test
    void mapsEveryDeckToResponseKeepingRepositoryOrder() {
        Deck withOneCard = new Deck("카드 있는 암기장", USER_ID);
        new Card("질문", "답", withOneCard);
        Deck empty = new Deck("빈 암기장", USER_ID);
        when(deckRepository.findAllByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of(withOneCard, empty));

        List<DeckResponseDto> result = deckService.getAllDecks(USER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("카드 있는 암기장");
        assertThat(result.get(0).getCardCount()).isEqualTo(1);
        assertThat(result.get(1).getTitle()).isEqualTo("빈 암기장");
        assertThat(result.get(1).getCardCount()).isZero();
    }

    @Test
    void throwsDeckNotFoundWhenUpdatingMissingDeck() {
        when(deckRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deckService.updateDeck(USER_ID, 99L, new DeckRequestDto("새 이름")))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DECK_NOT_FOUND);
    }

    @Test
    void throwsDeckNotFoundWhenDeletingMissingDeckWithoutDeleting() {
        when(deckRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deckService.deleteDeck(USER_ID, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DECK_NOT_FOUND);

        verify(deckRepository, never()).delete(any());
    }
}
