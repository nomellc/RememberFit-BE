package com.rememberfit.backend.domain.card.repository;

import com.rememberfit.backend.domain.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("SELECT c FROM Card c WHERE c.deck.id = :deckId AND (c.nextReviewDate <= :today OR c.nextReviewDate IS NULL)")
    List<Card> findDueCards(@Param("deckId") Long deckId, @Param("today")LocalDate today);

    // 새 카드 개수 세기
    long countByRepetition(int repetition);

    // 복습 카드 개수 세기
    long countByNextReviewDateLessThanEqual(LocalDate date);

    // 암기 완료 카드 개수 세기
    long countByRepetitionGreaterThanEqual(int repetition);
}
