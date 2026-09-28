package com.rememberfit.backend.domain.study.service;

import com.rememberfit.backend.domain.card.dto.CardResponseDto;
import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.card.repository.CardRepository;
import com.rememberfit.backend.domain.deck.repository.DeckRepository;
import com.rememberfit.backend.domain.study.dto.StudyGradeResponseDto;
import com.rememberfit.backend.domain.study.entity.StudyLog;
import com.rememberfit.backend.domain.study.repository.StudyLogRepository;
import com.rememberfit.backend.global.exception.CustomException;
import com.rememberfit.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final StudyLogRepository studyLogRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<CardResponseDto> getDueCards(Long deckId) {
        validateDeck(deckId);
        return cardRepository.findDueCards(deckId, LocalDate.now(clock)).stream()
                .map(CardResponseDto::new)
                .toList();
    }

    @Transactional
    public StudyGradeResponseDto gradeCard(Long deckId, Long cardId, int quality) {
        Card card = cardRepository.findByIdAndDeckId(cardId, deckId)
                .orElseThrow(() -> new CustomException(ErrorCode.CARD_NOT_FOUND));
        Sm2Calculator.Sm2Result result = Sm2Calculator.calculate(card, quality);
        LocalDate studyDate = LocalDate.now(clock);
        LocalDate nextReviewDate = studyDate.plusDays(result.interval());

        card.updateStudyStatus(
                result.repetition(),
                result.interval(),
                result.easeFactor(),
                nextReviewDate
        );
        studyLogRepository.save(new StudyLog(
                card.getId(),
                deckId,
                quality,
                result.repetition(),
                result.interval(),
                result.easeFactor(),
                nextReviewDate,
                studyDate,
                clock.instant()
        ));

        return new StudyGradeResponseDto(card);
    }

    private void validateDeck(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            throw new CustomException(ErrorCode.DECK_NOT_FOUND);
        }
    }
}
