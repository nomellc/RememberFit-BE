package com.rememberfit.backend.domain.deck.controller;

import com.rememberfit.backend.domain.deck.dto.DeckRequestDto;
import com.rememberfit.backend.domain.deck.dto.DeckResponseDto;
import com.rememberfit.backend.domain.deck.service.DeckService;
import com.rememberfit.backend.global.response.ApiSuccess;
import com.rememberfit.backend.global.response.SuccessCode;
import jakarta.validation.Valid;
import com.rememberfit.backend.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {
    private final DeckService deckService;

    @PostMapping
    @ApiSuccess(SuccessCode.DECK_CREATED)
    public DeckResponseDto createDeck(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody DeckRequestDto requestDto
    ) {
        return deckService.createDeck(user.id(), requestDto);
    }

    @GetMapping
    @ApiSuccess(SuccessCode.DECKS_READ)
    public List<DeckResponseDto> getDecks(@AuthenticationPrincipal AuthenticatedUser user) {
        return deckService.getAllDecks(user.id());
    }

    @PatchMapping("/{id}")
    @ApiSuccess(SuccessCode.DECK_UPDATED)
    public DeckResponseDto updateDeck(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody DeckRequestDto requestDto
    ) {
        return deckService.updateDeck(user.id(), id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ApiSuccess(SuccessCode.DECK_DELETED)
    public void deleteDeck(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        deckService.deleteDeck(user.id(), id);
    }
}
