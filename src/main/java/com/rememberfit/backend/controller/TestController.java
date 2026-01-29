package com.rememberfit.backend.controller;

import com.rememberfit.backend.dto.DeckRequestDto;
import com.rememberfit.backend.dto.DeckResponseDto;
import com.rememberfit.backend.entity.Card;
import com.rememberfit.backend.entity.Deck;
import com.rememberfit.backend.repository.CardRepository;
import com.rememberfit.backend.repository.DeckRepository;
import com.rememberfit.backend.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final DeckService deckService;

    @GetMapping("/test/create")
    public String createTestData() {
        // 덱 만들기
        DeckRequestDto request = new DeckRequestDto();
        return "서비스 연결 확인";
    }

    @GetMapping("/test/read")
    public List<DeckResponseDto> readTestData() {
        return deckService.getAllDecks();
    }
}
