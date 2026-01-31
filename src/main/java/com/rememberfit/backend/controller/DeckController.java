package com.rememberfit.backend.controller;

import com.rememberfit.backend.dto.DeckRequestDto;
import com.rememberfit.backend.dto.DeckResponseDto;
import com.rememberfit.backend.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {
    private final DeckService deckService;

    @PostMapping
    public String createDeck(@RequestBody DeckRequestDto requestDto) {
        deckService.createDeck(requestDto);
        return "덱 생성 성공!";
    }

    @GetMapping
    public List<DeckResponseDto> getDecks() {
        return deckService.getAllDecks();
    }

    @DeleteMapping("/{id}")
    public String deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return "덱 삭제 성공!";
    }
}
