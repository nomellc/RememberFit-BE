package com.rememberfit.backend.domain.card.service;

import com.rememberfit.backend.domain.card.dto.CardRequestDto;
import com.rememberfit.backend.domain.card.dto.CardResponseDto;
import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.card.repository.CardRepository;
import com.rememberfit.backend.domain.deck.entity.Deck;
import com.rememberfit.backend.domain.deck.repository.DeckRepository;
import com.rememberfit.backend.global.exception.CustomException;
import com.rememberfit.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    @Transactional
    public CardResponseDto createCard(Long userId, Long deckId, CardRequestDto requestDto) {
        Deck deck = getDeck(userId, deckId);
        Card card = new Card(
                requestDto.getFrontText().trim(),
                requestDto.getBackText().trim(),
                deck
        );
        return new CardResponseDto(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public List<CardResponseDto> getCardsByDeckId(Long userId, Long deckId) {
        getDeck(userId, deckId);
        return cardRepository.findAllByDeckIdOrderByIdDesc(deckId).stream()
                .map(CardResponseDto::new)
                .toList();
    }

    @Transactional
    public CardResponseDto updateCard(Long userId, Long deckId, Long cardId, CardRequestDto requestDto) {
        Card card = getCard(userId, deckId, cardId);
        card.updateContent(requestDto.getFrontText().trim(), requestDto.getBackText().trim());
        return new CardResponseDto(card);
    }

    @Transactional
    public void deleteCard(Long userId, Long deckId, Long cardId) {
        Card card = getCard(userId, deckId, cardId);
        card.getDeck().removeCard(card);
    }

    private Deck getDeck(Long userId, Long deckId) {
        return deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DECK_NOT_FOUND));
    }

    private Card getCard(Long userId, Long deckId, Long cardId) {
        return cardRepository.findByIdAndDeckIdAndDeck_UserId(cardId, deckId, userId)
                .orElseThrow(() -> cardLookupFailure(userId, deckId));
    }

    // 카드 조회 실패의 원인을 구분한다. 조회가 성공하는 정상 경로에는 쿼리를 더하지 않는다.
    private CustomException cardLookupFailure(Long userId, Long deckId) {
        if (!deckRepository.existsByIdAndUserId(deckId, userId)) {
            return new CustomException(ErrorCode.DECK_NOT_FOUND);
        }
        return new CustomException(ErrorCode.CARD_NOT_FOUND);
    }
}
