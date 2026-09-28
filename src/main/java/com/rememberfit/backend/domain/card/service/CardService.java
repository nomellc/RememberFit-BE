package com.rememberfit.backend.domain.card.service;

import com.rememberfit.backend.domain.card.dto.CardRequestDto;
import com.rememberfit.backend.domain.card.dto.CardResponseDto;
import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.deck.entity.Deck;
import com.rememberfit.backend.domain.card.repository.CardRepository;
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
    public CardResponseDto createCard(Long deckId, CardRequestDto requestDto) {
        Deck deck = getDeck(deckId);
        Card card = new Card(
                requestDto.getFrontText().trim(),
                requestDto.getBackText().trim(),
                deck
        );
        return new CardResponseDto(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public List<CardResponseDto> getCardsByDeckId(Long deckId) {
        getDeck(deckId);
        return cardRepository.findAllByDeckIdOrderByIdDesc(deckId).stream()
                .map(CardResponseDto::new)
                .toList();
    }

    @Transactional
    public CardResponseDto updateCard(Long deckId, Long cardId, CardRequestDto requestDto) {
        Card card = getCard(deckId, cardId);
        card.updateContent(requestDto.getFrontText().trim(), requestDto.getBackText().trim());
        return new CardResponseDto(card);
    }

    @Transactional
    public void deleteCard(Long deckId, Long cardId) {
        Card card = getCard(deckId, cardId);
        card.getDeck().removeCard(card);
    }

    private Deck getDeck(Long deckId) {
        return deckRepository.findById(deckId)
                .orElseThrow(() -> new CustomException(ErrorCode.DECK_NOT_FOUND));
    }

    private Card getCard(Long deckId, Long cardId) {
        return cardRepository.findByIdAndDeckId(cardId, deckId)
                .orElseThrow(() -> new CustomException(ErrorCode.CARD_NOT_FOUND));
    }
}
