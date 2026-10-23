package com.rememberfit.backend.domain.card.controller;

import com.rememberfit.backend.domain.card.dto.CardRequestDto;
import com.rememberfit.backend.domain.card.dto.CardResponseDto;
import com.rememberfit.backend.domain.card.service.CardService;
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
public class CardController {
    private final CardService cardService;

    @PostMapping("/{deckId}/cards")
    @ApiSuccess(SuccessCode.CARD_CREATED)
    public CardResponseDto createCard(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long deckId,
            @Valid @RequestBody CardRequestDto requestDto
    ) {
        return cardService.createCard(user.id(), deckId, requestDto);
    }

    @GetMapping("/{deckId}/cards")
    @ApiSuccess(SuccessCode.CARDS_READ)
    public List<CardResponseDto> getCards(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long deckId
    ) {
        return cardService.getCardsByDeckId(user.id(), deckId);
    }

    @PatchMapping("/{deckId}/cards/{cardId}")
    @ApiSuccess(SuccessCode.CARD_UPDATED)
    public CardResponseDto updateCard(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @Valid @RequestBody CardRequestDto requestDto
    ) {
        return cardService.updateCard(user.id(), deckId, cardId, requestDto);
    }

    @DeleteMapping("/{deckId}/cards/{cardId}")
    @ApiSuccess(SuccessCode.CARD_DELETED)
    public void deleteCard(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long deckId,
            @PathVariable Long cardId
    ) {
        cardService.deleteCard(user.id(), deckId, cardId);
    }
}
