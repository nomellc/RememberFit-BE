package com.rememberfit.backend.domain.card.controller;

import com.rememberfit.backend.domain.card.dto.CardGradeRequestDto;
import com.rememberfit.backend.domain.card.dto.CardRequestDto;
import com.rememberfit.backend.domain.card.dto.CardResponseDto;
import com.rememberfit.backend.domain.card.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping("/{deckId}/cards")
    public String createCard(@PathVariable Long deckId, @RequestBody CardRequestDto requestDto) {
        cardService.createCard(deckId, requestDto);
        return "카드 생성 성공!";
    }

    @GetMapping("/{deckId}/cards")
    public List<CardResponseDto> getCards(@PathVariable Long deckId) {
        return cardService.getCardsByDeckId(deckId);
    }

    @PostMapping("/{deckId}/cards/{cardId}/grade")
    public String gradeCard(@PathVariable Long cardId, @RequestBody CardGradeRequestDto requestDto, @PathVariable String deckId) {
        cardService.gradeCard(cardId, requestDto.getQuality());
        return "학습 기록 업데이트 완료!";
    }

    @GetMapping("/{deckId}/cards/due")
    public List<CardResponseDto> getDueCards(@PathVariable Long deckId) {
        return cardService.getDueCards(deckId);
    }
}
