package com.rememberfit.backend.global.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsBlankDeckTitleWithFieldError() throws Exception {
        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해주세요."))
                .andExpect(jsonPath("$.path").value("/api/decks"))
                .andExpect(jsonPath("$.fieldErrors.title").value("암기장 이름을 입력해주세요."));
    }

    @Test
    void rejectsInvalidCardFieldsBeforeLookingUpDeck() throws Exception {
        mockMvc.perform(post("/api/decks/99999/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"frontText\":\"\",\"backText\":\"답\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.frontText").value("카드 앞면을 입력해주세요."));
    }

    @Test
    void rejectsGradeOutsideSm2Range() throws Exception {
        mockMvc.perform(post("/api/decks/1/cards/1/grade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quality\":9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.quality").value("학습 점수는 5 이하여야 해요."));
    }

    @Test
    void returnsStructuredNotFoundError() throws Exception {
        mockMvc.perform(get("/api/decks/99999/cards"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DECK_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 암기장을 찾을 수 없어요."))
                .andExpect(jsonPath("$.fieldErrors").isMap());
    }

    @Test
    void returnsBadRequestForInvalidPathParameter() throws Exception {
        mockMvc.perform(get("/api/decks/not-a-number/cards"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PATH_PARAMETER"));
    }

    @Test
    void returnsStructuredErrorForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"));
    }
}
