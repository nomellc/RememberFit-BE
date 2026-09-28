package com.rememberfit.backend.domain.study.controller;

import com.rememberfit.backend.domain.card.dto.CardResponseDto;
import com.rememberfit.backend.domain.study.dto.StudyGradeRequestDto;
import com.rememberfit.backend.domain.study.dto.StudyGradeResponseDto;
import com.rememberfit.backend.domain.study.service.StudyService;
import com.rememberfit.backend.global.response.ApiSuccess;
import com.rememberfit.backend.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class StudyController {
    private final StudyService studyService;

    @GetMapping("/{deckId}/cards/due")
    @ApiSuccess(SuccessCode.DUE_CARDS_READ)
    public List<CardResponseDto> getDueCards(@PathVariable Long deckId) {
        return studyService.getDueCards(deckId);
    }

    @PostMapping("/{deckId}/cards/{cardId}/grade")
    @ApiSuccess(SuccessCode.STUDY_RECORDED)
    public StudyGradeResponseDto gradeCard(
            @PathVariable Long deckId,
            @PathVariable Long cardId,
            @Valid @RequestBody StudyGradeRequestDto requestDto
    ) {
        return studyService.gradeCard(deckId, cardId, requestDto.getQuality());
    }
}
