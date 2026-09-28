package com.rememberfit.backend.domain.deck.controller;

import com.rememberfit.backend.domain.deck.dto.DeckRequestDto;
import com.rememberfit.backend.domain.deck.dto.DeckResponseDto;
import com.rememberfit.backend.domain.deck.service.DeckService;
import com.rememberfit.backend.global.response.ApiSuccess;
import com.rememberfit.backend.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {
    private final DeckService deckService;

    @PostMapping
    @ApiSuccess(SuccessCode.DECK_CREATED)
    public DeckResponseDto createDeck(@Valid @RequestBody DeckRequestDto requestDto) {
        return deckService.createDeck(requestDto);
    }

    @GetMapping
    @ApiSuccess(SuccessCode.DECKS_READ)
    public List<DeckResponseDto> getDecks() {
        return deckService.getAllDecks();
    }

    @PatchMapping("/{id}")
    @ApiSuccess(SuccessCode.DECK_UPDATED)
    public DeckResponseDto updateDeck(
            @PathVariable Long id,
            @Valid @RequestBody DeckRequestDto requestDto
    ) {
        return deckService.updateDeck(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ApiSuccess(SuccessCode.DECK_DELETED)
    public void deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
    }
}
