package com.rememberfit.backend.service;

import com.rememberfit.backend.dto.DeckRequestDto;
import com.rememberfit.backend.dto.DeckResponseDto;
import com.rememberfit.backend.entity.Deck;
import com.rememberfit.backend.repository.DeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeckService {
    private final DeckRepository deckRepository;

    // 덱 생성
    @Transactional
    public void createDeck(DeckRequestDto requestDto) {
        Deck deck = new Deck(requestDto.getTitle());
        deckRepository.save(deck);
    }

    @Transactional(readOnly = true)
    public List<DeckResponseDto> getAllDecks() {
        List<Deck> decks = deckRepository.findAll();
        return decks.stream()
                .map(DeckResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDeck(Long id) {
        deckRepository.deleteById(id);
    }
}
