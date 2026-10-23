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
                .orElseThrow(() -> cardLookupFailure(deckId));
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

    // 카드 조회 실패의 원인을 구분한다. 덱 자체가 없으면 DECK_NOT_FOUND 가 맞다.
    // 조회가 성공하는 정상 경로에서는 덱 존재 확인 쿼리를 더하지 않는다.
    private CustomException cardLookupFailure(Long deckId) {
        if (!deckRepository.existsById(deckId)) {
            return new CustomException(ErrorCode.DECK_NOT_FOUND);
        }
        return new CustomException(ErrorCode.CARD_NOT_FOUND);
    }
}
