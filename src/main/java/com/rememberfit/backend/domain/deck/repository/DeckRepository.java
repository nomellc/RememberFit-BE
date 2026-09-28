package com.rememberfit.backend.domain.deck.repository;

import com.rememberfit.backend.domain.deck.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findAllByOrderByCreatedAtDesc();
}
