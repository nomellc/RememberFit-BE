package com.rememberfit.backend.domain.study;

import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.card.repository.CardRepository;
import com.rememberfit.backend.domain.deck.entity.Deck;
import com.rememberfit.backend.domain.deck.repository.DeckRepository;
import com.rememberfit.backend.domain.study.repository.StudyLogRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudyFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private StudyLogRepository studyLogRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void recordsGradeAndKeepsHistoryAfterDeckDeletion() throws Exception {
        Deck deck = deckRepository.saveAndFlush(new Deck("학습 기록 테스트"));
        Card card = cardRepository.saveAndFlush(new Card("질문", "답", deck));

        mockMvc.perform(post("/api/decks/{deckId}/cards/{cardId}/grade", deck.getId(), card.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cardId").value(card.getId()))
                .andExpect(jsonPath("$.data.intervalDays").value(1));

        assertThat(studyLogRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/api/decks/{deckId}", deck.getId()))
                .andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();

        assertThat(cardRepository.existsById(card.getId())).isFalse();
        assertThat(studyLogRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/statistics/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudyCount").value(1))
                .andExpect(jsonPath("$.data.qualityDistribution.goodCount").value(1));
    }
}
