package tn.esprit.matching.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.matching.dto.FreelancerMatchDTO;
import tn.esprit.matching.dto.FreelancerMatchedProjectDTO;
import tn.esprit.matching.dto.MatchingResultDTO;
import tn.esprit.matching.service.CollectDataService;
import tn.esprit.matching.service.MatchingService;
import tn.esprit.matching.service.ScoreService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchingController.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollectDataService collectDataService;

    @MockBean
    private MatchingService matchingService;

    @MockBean
    private ScoreService scoreService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getFullMatching_shouldReturnComputedResult() throws Exception {
        Long userId = 1L;
        Long projectId = 10L;

        // on simule un package de données minimal
        var dataPackage = new CollectDataService.MatchingDataPackage(
                null, null, null, List.of(), null, List.of(), null
        );
        given(collectDataService.getAllData(userId, projectId))
                .willReturn(CompletableFuture.completedFuture(dataPackage));

        given(scoreService.scoreAvailability(any())).willReturn(50.0);
        given(scoreService.scoreEducation(any(), any())).willReturn(60.0);
        given(scoreService.scoreSkills(any(), any())).willReturn(80.0);
        given(scoreService.scoreExperience(any(), any())).willReturn(70.0);
        given(scoreService.calculateFinalScore(80.0, 70.0, 60.0, 50.0))
                .willReturn(75.0);

        mockMvc.perform(get("/matching/score/full/{userId}/{projectId}", userId, projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.projectId", is(10)))
                .andExpect(jsonPath("$.skillsScore", is(80.0)))
                .andExpect(jsonPath("$.experienceScore", is(70.0)))
                .andExpect(jsonPath("$.educationScore", is(60.0)))
                .andExpect(jsonPath("$.availabilityScore", is(50.0)))
                .andExpect(jsonPath("$.finalScore", is(75.0)));
    }

    @Test
    void getMatchingForProject_shouldCallServiceWithTokenAndBody() throws Exception {
        Long projectId = 10L;
        List<Long> freelancerIds = List.of(1L, 2L);

        FreelancerMatchDTO dto1 = new FreelancerMatchDTO(
                1L, "John", "Doe", "Dev", "Dev",
                "FULL", List.of("Java"), 90.0
        );
        FreelancerMatchDTO dto2 = new FreelancerMatchDTO(
                2L, "Jane", "Smith", "Dev", "Dev",
                "PART", List.of("Spring"), 80.0
        );

        given(matchingService.getMatchingForProject(eq(projectId), eq(freelancerIds), anyString()))
                .willReturn(List.of(dto1, dto2));

        mockMvc.perform(post("/matching/project/{projectId}", projectId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(freelancerIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                // dans ton JSON, l'id du freelancer est dans le champ "id"
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    void getMatchingByProject_shouldCallServiceAutoWithToken() throws Exception {
        Long projectId = 10L;

        FreelancerMatchDTO dto = new FreelancerMatchDTO(
                1L, "John", "Doe", "Dev", "Dev",
                "FULL", List.of("Java"), 95.0
        );

        given(matchingService.getMatchingForProjectAuto(eq(projectId), anyString()))
                .willReturn(List.of(dto));

        mockMvc.perform(get("/matching/{projectId}", projectId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                // le JSON contient "id" pour l'identifiant du freelancer
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].matchScore", is(95.0)));
    }

    @Test
    void getMatchedProjectScores_shouldReturnList() throws Exception {
        FreelancerMatchedProjectDTO dto1 =
                new FreelancerMatchedProjectDTO(10L, 80.0);
        FreelancerMatchedProjectDTO dto2 =
                new FreelancerMatchedProjectDTO(20L, 70.0);

        given(matchingService.getMatchedProjectIdsForFreelancer(1L))
                .willReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/matching/freelancer/{freelancerId}/project-scores", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].projectId", is(10)))
                // dans ton JSON, le champ est "matchScore", pas "score"
                .andExpect(jsonPath("$[0].matchScore", is(80.0)));
    }
}