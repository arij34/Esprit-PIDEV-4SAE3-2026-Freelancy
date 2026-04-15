package tn.esprit.matching.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.matching.dto.FormResponseRequest;
import tn.esprit.matching.entity.ApplicationResponse;
import tn.esprit.matching.service.ApplicationResponseService;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationResponseController.class)
class ApplicationResponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationResponseService applicationResponseService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- POST /form-response : SUCCESS ----------

    @Test
    void save_whenServiceSucceeds_shouldReturn200AndBody() throws Exception {
        // given
        FormResponseRequest req = new FormResponseRequest();
        req.setInvitationId(1L);
        req.setQ1("Motivation");
        req.setQ2("Texte Q2");

        ApplicationResponse saved = new ApplicationResponse();
        saved.setId(10L);
        saved.setInvitationId(1L);
        saved.setAnswerQ1("Motivation");

        when(applicationResponseService.saveResponse(any(FormResponseRequest.class)))
                .thenReturn(saved);

        // when + then
        mockMvc.perform(post("/form-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.invitationId", is(1)))
                .andExpect(jsonPath("$.answerQ1", is("Motivation")));
    }

    // ---------- POST /form-response : IllegalArgumentException -> 400 ----------

    @Test
    void save_whenServiceThrowsIllegalArgument_shouldReturn400WithErrorMessage() throws Exception {
        // given
        FormResponseRequest req = new FormResponseRequest();
        req.setInvitationId(1L);
        req.setQ1("x".repeat(3000)); // trop long, peu importe ici

        when(applicationResponseService.saveResponse(any(FormResponseRequest.class)))
                .thenThrow(new IllegalArgumentException("Validation error"));

        // when + then
        mockMvc.perform(post("/form-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation error")));
    }

    // ---------- POST /form-response : IllegalStateException -> 403 ----------

    @Test
    void save_whenServiceThrowsIllegalState_shouldReturn403WithErrorMessage() throws Exception {
        // given
        FormResponseRequest req = new FormResponseRequest();
        req.setInvitationId(1L);

        when(applicationResponseService.saveResponse(any(FormResponseRequest.class)))
                .thenThrow(new IllegalStateException("Too late to edit"));

        // when + then
        mockMvc.perform(post("/form-response")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Too late to edit")));
    }

    // ---------- GET /form-response/{invitationId} : found -> 200 ----------

    @Test
    void getByInvitation_whenResponseExists_shouldReturn200WithBody() throws Exception {
        // given
        ApplicationResponse resp = new ApplicationResponse();
        resp.setId(5L);
        resp.setInvitationId(1L);
        resp.setAnswerQ1("Motivation");
        resp.setCreatedAt(LocalDateTime.now());

        given(applicationResponseService.getByInvitationId(1L))
                .willReturn(resp);

        // when + then
        mockMvc.perform(get("/form-response/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.invitationId", is(1)))
                .andExpect(jsonPath("$.answerQ1", is("Motivation")));
    }

    // ---------- GET /form-response/{invitationId} : not found -> 404 ----------

    @Test
    void getByInvitation_whenResponseDoesNotExist_shouldReturn404() throws Exception {
        // given
        given(applicationResponseService.getByInvitationId(99L))
                .willReturn(null);

        // when + then
        mockMvc.perform(get("/form-response/99"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /form-response/{invitationId}/can-edit ----------

    @Test
    void canEdit_shouldReturnCanEditFlagFromService() throws Exception {
        // given
        given(applicationResponseService.canEdit(1L))
                .willReturn(true);

        // when + then
        mockMvc.perform(get("/form-response/1/can-edit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit", is(true)));
    }
}