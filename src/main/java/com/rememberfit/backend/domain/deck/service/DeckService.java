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
    public DeckResponseDto createDeck(Long userId, DeckRequestDto requestDto) {
        Deck deck = new Deck(requestDto.getTitle().trim(), userId);
        return new DeckResponseDto(deckRepository.save(deck));
    }

    @Transactional(readOnly = true)
    public List<DeckResponseDto> getAllDecks(Long userId) {
        return deckRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(DeckResponseDto::new)
                .toList();
    }

    @Transactional
    public DeckResponseDto updateDeck(Long userId, Long deckId, DeckRequestDto requestDto) {
        Deck deck = getDeck(userId, deckId);
        deck.updateTitle(requestDto.getTitle().trim());
        return new DeckResponseDto(deck);
    }

    @Transactional
    public void deleteDeck(Long userId, Long deckId) {
        deckRepository.delete(getDeck(userId, deckId));
    }

    /**
     * 남의 암기장을 요청하면 403 이 아니라 404 를 돌려준다.
     * 403 은 "그 암기장이 존재한다"는 사실을 알려주는 셈이 된다.
     */
    private Deck getDeck(Long userId, Long deckId) {
        return deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DECK_NOT_FOUND));
    }
}
