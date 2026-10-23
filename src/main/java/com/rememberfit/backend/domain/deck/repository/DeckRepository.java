package com.rememberfit.backend.domain.deck.repository;

import com.rememberfit.backend.domain.deck.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Deck> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
