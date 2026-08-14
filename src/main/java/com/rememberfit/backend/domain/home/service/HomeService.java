package com.rememberfit.backend.domain.home.service;

import com.rememberfit.backend.domain.home.dto.HomeStatsResponseDto;
import com.rememberfit.backend.domain.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final CardRepository cardRepository;

    @Transactional(readOnly = true)
    public HomeStatsResponseDto getHomeStats() {
        // 오늘 날짜 구하기
        LocalDate today = LocalDate.now();

        // DB에서 숫자 세오기
        long newCount = cardRepository.countByRepetition(0);
        long reviewCount = cardRepository.countByNextReviewDateLessThanEqual(today);
        long doneCount = cardRepository.countByRepetitionGreaterThanEqual(3); // 3번 이상 맞추면 암기 완료

        // DTO에 담아서 반환
        return new HomeStatsResponseDto(newCount, reviewCount, doneCount);
    }
}
