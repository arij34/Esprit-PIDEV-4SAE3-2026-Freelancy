package tn.esprit.challengeservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.challengeservice.entities.Challenge;
import tn.esprit.challengeservice.services.ichallengeService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChallengeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ichallengeService challengeService;

    @Test
    void addChallenge_shouldReturnSavedChallenge() throws Exception {
        Challenge challenge = Challenge.builder().idChallenge("ch-1").title("Docker").build();
        when(challengeService.addChallenge(any(Challenge.class))).thenReturn(challenge);

        mockMvc.perform(post("/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(challenge)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idChallenge").value("ch-1"));
    }

    @Test
    void getAllChallenges_shouldReturnOk() throws Exception {
        when(challengeService.getAllChallenges()).thenReturn(List.of());

        mockMvc.perform(get("/challenges"))
                .andExpect(status().isOk());
    }

    @Test
    void getActiveChallengesCount_shouldReturnCount() throws Exception {
        when(challengeService.countActiveChallenges()).thenReturn(5L);

        mockMvc.perform(get("/challenges/count/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    void getCompletedChallengesCount_shouldReturnCount() throws Exception {
        when(challengeService.countCompletedChallenges()).thenReturn(2L);

        mockMvc.perform(get("/challenges/count/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    void getTechnologyCounts_shouldReturnMap() throws Exception {
        when(challengeService.getTechnologyCounts()).thenReturn(Map.of("Docker", 3L));

        mockMvc.perform(get("/challenges/stats/technology-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Docker").value(3));
    }

    @Test
    void getCategoryCounts_shouldReturnMap() throws Exception {
        when(challengeService.getCategoryCounts()).thenReturn(Map.of("DevOps", 4L));

        mockMvc.perform(get("/challenges/stats/category-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.DevOps").value(4));
    }

    @Test
    void getChallengeById_shouldReturnChallenge() throws Exception {
        Challenge challenge = Challenge.builder().idChallenge("ch-1").title("Docker").build();
        when(challengeService.getChallengeById("ch-1")).thenReturn(challenge);

        mockMvc.perform(get("/challenges/ch-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Docker"));
    }

    @Test
    void updateChallenge_shouldReturnUpdatedChallenge() throws Exception {
        Challenge challenge = Challenge.builder().idChallenge("ch-1").title("Updated").build();
        when(challengeService.updateChallenge(any(String.class), any(Challenge.class))).thenReturn(challenge);

        mockMvc.perform(put("/challenges/ch-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(challenge)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deleteChallenge_shouldReturnOk() throws Exception {
        doNothing().when(challengeService).deleteChallenge("ch-1");

        mockMvc.perform(delete("/challenges/ch-1"))
                .andExpect(status().isOk());
    }
}
