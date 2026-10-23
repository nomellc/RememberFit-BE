package com.rememberfit.backend.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 로그인을 붙인 진짜 이유를 검증한다.
 * 다른 사용자의 암기장은 보이지도 않고, 주소를 직접 알아도 건드릴 수 없어야 한다.
 * 존재 여부까지 숨기기 위해 403 이 아니라 404 로 답한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DataIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String ownerToken;
    private String intruderToken;
    private long ownerDeckId;
    private long ownerCardId;

    @BeforeEach
    void setUpTwoUsers() throws Exception {
        ownerToken = signUpAndGetToken("owner@test.local", "주인");
        intruderToken = signUpAndGetToken("intruder@test.local", "침입자");

        String deckBody = mockMvc.perform(post("/api/decks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"주인의 암기장\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ownerDeckId = objectMapper.readTree(deckBody).get("data").get("id").asLong();

        String cardBody = mockMvc.perform(post("/api/decks/{deckId}/cards", ownerDeckId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"비밀 질문\",\"backText\":\"비밀 답\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        ownerCardId = objectMapper.readTree(cardBody).get("data").get("id").asLong();
    }

    @Test
    void ownerSeesOwnDeckAndIntruderSeesNothing() throws Exception {
        mockMvc.perform(get("/api/decks").header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("주인의 암기장"));

        mockMvc.perform(get("/api/decks").header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void intruderCannotReadAnotherUsersCards() throws Exception {
        mockMvc.perform(get("/api/decks/{deckId}/cards", ownerDeckId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DECK_NOT_FOUND"));

        mockMvc.perform(get("/api/decks/{deckId}/cards/due", ownerDeckId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DECK_NOT_FOUND"));
    }

    @Test
    void intruderCannotModifyOrDeleteAnotherUsersDeck() throws Exception {
        mockMvc.perform(patch("/api/decks/{deckId}", ownerDeckId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"가로채기\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DECK_NOT_FOUND"));

        mockMvc.perform(delete("/api/decks/{deckId}", ownerDeckId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken))
                .andExpect(status().isNotFound());

        // 주인에게는 그대로 남아 있어야 한다.
        mockMvc.perform(get("/api/decks").header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("주인의 암기장"));
    }

    @Test
    void intruderCannotModifyOrGradeAnotherUsersCard() throws Exception {
        mockMvc.perform(patch("/api/decks/{deckId}/cards/{cardId}", ownerDeckId, ownerCardId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"변조\",\"backText\":\"변조\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DECK_NOT_FOUND"));

        mockMvc.perform(post("/api/decks/{deckId}/cards/{cardId}/grade", ownerDeckId, ownerCardId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\":5}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DECK_NOT_FOUND"));

        mockMvc.perform(delete("/api/decks/{deckId}/cards/{cardId}", ownerDeckId, ownerCardId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void statisticsCountOnlyTheRequestingUsersData() throws Exception {
        mockMvc.perform(post("/api/decks/{deckId}/cards/{cardId}/grade", ownerDeckId, ownerCardId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\":5}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/statistics/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewCount").value(0))
                .andExpect(jsonPath("$.data.newCount").value(0));

        // 침입자의 통계에는 주인의 카드도, 주인의 학습 기록도 들어가면 안 된다.
        mockMvc.perform(get("/api/statistics/summary")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newCount").value(0))
                .andExpect(jsonPath("$.data.reviewCount").value(0))
                .andExpect(jsonPath("$.data.doneCount").value(0));

        mockMvc.perform(get("/api/statistics/insights")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudyCount").value(1));

        mockMvc.perform(get("/api/statistics/insights")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + intruderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStudyCount").value(0))
                .andExpect(jsonPath("$.data.streakDays").value(0));
    }

    private String signUpAndGetToken(String email, String nickname) throws Exception {
        String body = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email, "password", "password1234", "nickname", nickname))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("data").get("accessToken").asText();
    }
}
