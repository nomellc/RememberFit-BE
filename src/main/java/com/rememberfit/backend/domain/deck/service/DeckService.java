package com.rememberfit.backend.domain.deck.service;

import com.rememberfit.backend.domain.deck.dto.DeckRequestDto;
import com.rememberfit.backend.domain.deck.dto.DeckResponseDto;
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
public class DeckService {
    private final DeckRepository deckRepository;

    @Transactional
    public DeckResponseDto createDeck(DeckRequestDto requestDto) {
        Deck deck = new Deck(requestDto.getTitle().trim());
        return new DeckResponseDto(deckRepository.save(deck));
    }

    @Transactional(readOnly = true)
    public List<DeckResponseDto> getAllDecks() {
        return deckRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(DeckResponseDto::new)
                .toList();
    }

    @Transactional
    public DeckResponseDto updateDeck(Long deckId, DeckRequestDto requestDto) {
        Deck deck = getDeck(deckId);
        deck.updateTitle(requestDto.getTitle().trim());
        return new DeckResponseDto(deck);
    }

    @Transactional
    public void deleteDeck(Long deckId) {
        deckRepository.delete(getDeck(deckId));
    }

    private Deck getDeck(Long deckId) {
        return deckRepository.findById(deckId)
                .orElseThrow(() -> new CustomException(ErrorCode.DECK_NOT_FOUND));
    }
}
