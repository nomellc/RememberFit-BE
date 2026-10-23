package com.rememberfit.backend.domain.card.service;

import com.rememberfit.backend.domain.card.dto.CardRequestDto;
import com.rememberfit.backend.domain.card.repository.CardRepository;
import com.rememberfit.backend.domain.deck.repository.DeckRepository;
import com.rememberfit.backend.global.exception.CustomException;
import com.rememberfit.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CardServiceTest {
    private static final Long USER_ID = 7L;

    private CardRepository cardRepository;
    private DeckRepository deckRepository;
    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        deckRepository = mock(DeckRepository.class);
        cardService = new CardService(cardRepository, deckRepository);
    }

    @Test
    void throwsDeckNotFoundBeforeQueryingCardsWhenDeckIsMissing() {
        when(deckRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardsByDeckId(USER_ID, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DECK_NOT_FOUND);

        verifyNoInteractions(cardRepository);
    }

    @Test
    void throwsDeckNotFoundWhenCreatingCardInMissingDeck() {
        when(deckRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(USER_ID, 99L, new CardRequestDto()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DECK_NOT_FOUND);

        verify(cardRepository, never()).save(any());
    }
}
