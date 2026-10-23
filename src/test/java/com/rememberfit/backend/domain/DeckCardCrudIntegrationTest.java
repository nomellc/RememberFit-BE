package com.rememberfit.backend.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rememberfit.backend.domain.card.entity.Card;
import com.rememberfit.backend.domain.card.repository.CardRepository;
import com.rememberfit.backend.domain.deck.entity.Deck;
import com.rememberfit.backend.domain.deck.repository.DeckRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.rememberfit.backend.domain.user.entity.User;
import com.rememberfit.backend.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.rememberfit.backend.support.AuthTestSupport.asUser;
import static com.rememberfit.backend.support.AuthTestSupport.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeckCardCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void createOwner() {
        user = createUser(userRepository, "deck-owner@test.local");
    }

    @Test
    void supportsDeckAndCardCreateReadUpdateDeleteFlow() throws Exception {
        String deckBody = mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" 영어 단어 \"}")
                        .with(asUser(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("영어 단어"))
                .andReturn().getResponse().getContentAsString();
        JsonNode deckJson = objectMapper.readTree(deckBody);
        long deckId = deckJson.get("data").get("id").asLong();

        String cardBody = mockMvc.perform(post("/api/decks/{deckId}/cards", deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\" apple \",\"backText\":\" 사과 \"}")
                        .with(asUser(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.frontText").value("apple"))
                .andExpect(jsonPath("$.data.backText").value("사과"))
                .andReturn().getResponse().getContentAsString();
        long cardId = objectMapper.readTree(cardBody).get("data").get("id").asLong();

        mockMvc.perform(patch("/api/decks/{deckId}", deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"기초 영어\"}")
                        .with(asUser(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("기초 영어"));

        mockMvc.perform(patch("/api/decks/{deckId}/cards/{cardId}", deckId, cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"banana\",\"backText\":\"바나나\"}")
                        .with(asUser(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.frontText").value("banana"))
                .andExpect(jsonPath("$.data.backText").value("바나나"));

        mockMvc.perform(post("/api/decks/{deckId}/cards", deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"cherry\",\"backText\":\"체리\"}")
                        .with(asUser(user)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/decks/{deckId}/cards", deckId)
                        .with(asUser(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].frontText").value("cherry"))
                .andExpect(jsonPath("$.data[1].id").value(cardId))
                .andExpect(jsonPath("$.data[1].frontText").value("banana"));

        mockMvc.perform(delete("/api/decks/{deckId}/cards/{cardId}", deckId, cardId)
                        .with(asUser(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("카드를 삭제했어요."));

        assertThat(cardRepository.existsById(cardId)).isFalse();
    }

    @Test
    void deletingDeckAlsoDeletesItsCards() throws Exception {
        Deck deck = deckRepository.saveAndFlush(new Deck("삭제할 암기장", user.getId()));
        Card card = cardRepository.saveAndFlush(new Card("질문", "답", deck));

        mockMvc.perform(delete("/api/decks/{deckId}", deck.getId())
                        .with(asUser(user)))
                .andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();

        assertThat(deckRepository.existsById(deck.getId())).isFalse();
        assertThat(cardRepository.existsById(card.getId())).isFalse();
    }

    @Test
    void rejectsCardMutationThroughAnotherDeck() throws Exception {
        Deck ownerDeck = deckRepository.saveAndFlush(new Deck("원래 암기장", user.getId()));
        Deck otherDeck = deckRepository.saveAndFlush(new Deck("다른 암기장", user.getId()));
        Card card = cardRepository.saveAndFlush(new Card("원래 질문", "원래 답", ownerDeck));

        mockMvc.perform(patch("/api/decks/{deckId}/cards/{cardId}", otherDeck.getId(), card.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"변경\",\"backText\":\"차단\"}")
                        .with(asUser(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CARD_NOT_FOUND"));

        mockMvc.perform(post("/api/decks/{deckId}/cards/{cardId}/grade", otherDeck.getId(), card.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\":5}")
                        .with(asUser(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CARD_NOT_FOUND"));

        entityManager.clear();
        Card unchanged = cardRepository.findById(card.getId()).orElseThrow();
        assertThat(unchanged.getFrontText()).isEqualTo("원래 질문");
        assertThat(unchanged.getRepetition()).isZero();
    }

    @Test
    void listsDecksWithCardCountThroughApi() throws Exception {
        Deck deck = deckRepository.saveAndFlush(new Deck("목록 조회 테스트", user.getId()));
        cardRepository.saveAndFlush(new Card("질문", "답", deck));

        mockMvc.perform(get("/api/decks")
                        .with(asUser(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data[0].id").value(deck.getId()))
                .andExpect(jsonPath("$.data[0].title").value("목록 조회 테스트"))
                .andExpect(jsonPath("$.data[0].cardCount").value(1));
    }

    @Test
    void rejectsDeckUpdateWhenDeckIsMissing() throws Exception {
        mockMvc.perform(patch("/api/decks/{deckId}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"없는 암기장\"}")
                        .with(asUser(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DECK_NOT_FOUND"));
    }

    @Test
    void rejectsCardListingWhenDeckIsMissing() throws Exception {
        mockMvc.perform(get("/api/decks/{deckId}/cards", 999999L)
                        .with(asUser(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DECK_NOT_FOUND"));
    }
}
