package com.rememberfit.backend.service;

import com.rememberfit.backend.dto.CardRequestDto;
import com.rememberfit.backend.dto.CardResponseDto;
import com.rememberfit.backend.entity.Card;
import com.rememberfit.backend.entity.Deck;
import com.rememberfit.backend.repository.CardRepository;
import com.rememberfit.backend.repository.DeckRepository;
import com.rememberfit.backend.utils.SM2Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;

    @Transactional
    public void createCard(Long deckId, CardRequestDto requestDto) {
        // 덱 조회
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 덱이 없습니다."));

        // 카드 생성
        Card card = new Card(requestDto.getFrontText(), requestDto.getBackText(), deck);

        // 저장
        cardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public List<CardResponseDto> getCardsByDeckId(Long deckId) {
        // 덱 확인
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 덱이 없습니다."));

        // 그 덱에 들어있는 카드 리스트 가져오기
        List<Card> cards = deck.getCards();

        return cards.stream()
                .map(CardResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void gradeCard(Long cardId, int quality) {
        // 카드 조회
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("카드 없음"));

        // 알고리즘 계산
        SM2Utils.SM2Result result = SM2Utils.calculate(card, quality);

        // 다음 학습 날짜 계산
        LocalDate nextDate = LocalDate.now().plusDays(result.interval);

        // 카드 정보 업데이트
        card.updateStudyStatus(result.repetition, result.interval, result.easeFactor, nextDate);
    }

    @Transactional(readOnly = true)
    public List<CardResponseDto> getDueCards(Long deckId) {
        // 오늘 날짜 구하기
        LocalDate today = LocalDate.now();

        // repository 호출
        List<Card> cards = cardRepository.findDueCards(deckId, today);

        // DTO 변환
        return cards.stream()
                .map(CardResponseDto::new)
                .collect(Collectors.toList());
    }
}
