package tn.esprit.challengeservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.challengeservice.entities.ChallengeParticipation;
import tn.esprit.challengeservice.entities.ParticipationStatus;
import tn.esprit.challengeservice.entities.SonarCloudResult;
import tn.esprit.challengeservice.exceptions.SonarResultsNotFoundException;
import tn.esprit.challengeservice.services.GitHubService;
import tn.esprit.challengeservice.services.iparticipationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParticipationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ParticipationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private iparticipationService participationService;

    @MockBean
    private GitHubService gitHubService;

    @Test
    void joinChallenge_shouldReturnParticipation() throws Exception {
        ChallengeParticipation participation = ChallengeParticipation.builder().id("p-1").status(ParticipationStatus.ACTIVE).build();
        when(participationService.joinChallenge("ch-1", "gh-user", "Bearer abc")).thenReturn(participation);

        mockMvc.perform(post("/participations/ch-1/join")
                        .param("usernameGithub", "gh-user")
                        .header("Authorization", "Bearer abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"));
    }

    @Test
    void getMyParticipations_shouldReturnList() throws Exception {
        when(participationService.getMyParticipations("Bearer abc"))
                .thenReturn(List.of(ChallengeParticipation.builder().id("p-1").build()));

        mockMvc.perform(get("/participations/my/challenges").header("Authorization", "Bearer abc"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyParticipationForChallenge_shouldReturnParticipation() throws Exception {
        when(participationService.getMyParticipationForChallenge("ch-1", "Bearer abc"))
                .thenReturn(ChallengeParticipation.builder().id("p-1").build());

        mockMvc.perform(get("/participations/my/challenge/ch-1").header("Authorization", "Bearer abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"));
    }

    @Test
    void getAllParticipations_shouldReturnOk() throws Exception {
        when(participationService.getAllParticipations()).thenReturn(List.of());

        mockMvc.perform(get("/participations"))
                .andExpect(status().isOk());
    }

    @Test
    void getTotalParticipantsCount_shouldReturnCount() throws Exception {
        when(participationService.getTotalParticipantsCount()).thenReturn(9L);

        mockMvc.perform(get("/participations/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(9));
    }

    @Test
    void getParticipationById_shouldReturnParticipation() throws Exception {
        when(participationService.getParticipationById("p-1"))
                .thenReturn(ChallengeParticipation.builder().id("p-1").build());

        mockMvc.perform(get("/participations/p-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p-1"));
    }

    @Test
    void getParticipationsByChallenge_shouldReturnList() throws Exception {
        when(participationService.getParticipationsByChallenge("ch-1")).thenReturn(List.of());

        mockMvc.perform(get("/participations/challenge/ch-1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteParticipation_shouldReturnOk() throws Exception {
        doNothing().when(participationService).deleteParticipation("p-1");

        mockMvc.perform(delete("/participations/p-1"))
                .andExpect(status().isOk());
    }

    @Test
    void checkInvitationStatus_shouldReturnAcceptedFlag() throws Exception {
        when(participationService.checkInvitationStatus("p-1")).thenReturn(true);

        mockMvc.perform(get("/participations/p-1/invitation-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    void confirmInvitation_whenNotAccepted_shouldReturnBadRequest() throws Exception {
        when(participationService.checkInvitationStatus("p-1")).thenReturn(false);

        mockMvc.perform(post("/participations/p-1/confirm-invitation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.accepted").value(false));
    }

    @Test
    void confirmInvitation_whenAccepted_shouldReturnOk() throws Exception {
        when(participationService.checkInvitationStatus("p-1")).thenReturn(true);

        mockMvc.perform(post("/participations/p-1/confirm-invitation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    void submitChallenge_shouldReturnPrUrl() throws Exception {
        when(participationService.submitChallenge("p-1", "feature")).thenReturn("https://github.com/pr/1");

        mockMvc.perform(post("/participations/p-1/submit").param("branchName", "feature"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pullRequestUrl").value("https://github.com/pr/1"));
    }

    @Test
    void fetchSonarResultsByPrUrl_shouldReturnParsedMetrics() throws Exception {
        when(gitHubService.fetchSonarCloudMetricsByPrUrl("http://pr"))
                .thenReturn(Map.of(
                        "alert_status", "OK",
                        "bugs", "1",
                        "code_smells", "2",
                        "vulnerabilities", "3",
                        "security_hotspots", "4",
                        "coverage", "75.5",
                        "duplicated_lines_density", "1.2",
                        "ncloc", "200"
                ));

        mockMvc.perform(get("/participations/sonar-results/fetch-by-url").param("prUrl", "http://pr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qualityGateStatus").value("OK"))
                .andExpect(jsonPath("$.bugs").value(1));
    }

    @Test
    void getSonarResults_whenAlreadyPresent_shouldReturnResult() throws Exception {
        SonarCloudResult result = SonarCloudResult.builder().id("sr-1").qualityGateStatus("OK").build();
        when(participationService.getSonarResults("p-1")).thenReturn(result);

        mockMvc.perform(get("/participations/p-1/sonar-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sr-1"));
    }

    @Test
    void getSonarResults_whenMissing_thenFetchShouldReturnResult() throws Exception {
        SonarCloudResult result = SonarCloudResult.builder().id("sr-2").qualityGateStatus("OK").build();
        when(participationService.getSonarResults("p-1")).thenThrow(new SonarResultsNotFoundException("not ready"));
        when(participationService.fetchSonarResults("p-1")).thenReturn(result);

        mockMvc.perform(get("/participations/p-1/sonar-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sr-2"));
    }

    @Test
    void getSonarResults_whenMissing_andFetchFails_shouldReturnNotFound() throws Exception {
        when(participationService.getSonarResults("p-1")).thenThrow(new SonarResultsNotFoundException("not ready"));
        when(participationService.fetchSonarResults("p-1")).thenThrow(new RuntimeException("fetch failed"));

        mockMvc.perform(get("/participations/p-1/sonar-results"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not ready"));
    }

    @Test
    void getSonarResultsStatus_whenPresent_shouldReturnCompleted() throws Exception {
        SonarCloudResult result = SonarCloudResult.builder().id("sr-1").build();
        when(participationService.getSonarResultsIfPresent("p-1")).thenReturn(Optional.of(result));

        mockMvc.perform(get("/participations/p-1/sonar-results/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    void getSonarResultsStatus_whenAbsent_shouldReturnPending() throws Exception {
        when(participationService.getSonarResultsIfPresent("p-1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/participations/p-1/sonar-results/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    void refreshSonarResults_shouldReturnResult() throws Exception {
        SonarCloudResult result = SonarCloudResult.builder().id("sr-1").build();
        when(participationService.fetchSonarResults("p-1")).thenReturn(result);

        mockMvc.perform(post("/participations/p-1/sonar-results/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sr-1"));
    }

    @Test
    void updateSonarResults_shouldReturnResult() throws Exception {
        SonarCloudResult input = SonarCloudResult.builder().qualityGateStatus("OK").build();
        SonarCloudResult saved = SonarCloudResult.builder().id("sr-1").qualityGateStatus("OK").build();
        when(participationService.updateSonarResults(any(String.class), any(SonarCloudResult.class))).thenReturn(saved);

        mockMvc.perform(put("/participations/p-1/sonar-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("sr-1"));
    }

    @Test
    void saveSonarPoints_shouldReturnOk() throws Exception {
        doNothing().when(participationService).saveSonarPoints("p-1", "sr-1", 50);

        mockMvc.perform(patch("/participations/p-1/sonar-results/sr-1/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pointsAwarded\":50}"))
                .andExpect(status().isOk());
    }

    @Test
    void saveSonarPoints_whenMissingPoints_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/participations/p-1/sonar-results/sr-1/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("pointsAwarded is required"));
    }

    @Test
    void checkGitHubUserExists_shouldReturnResult() throws Exception {
        when(gitHubService.doesUserExist("octocat")).thenReturn(true);

        mockMvc.perform(get("/participations/github/user-exists/octocat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void checkBranchExists_shouldReturnResult() throws Exception {
        when(gitHubService.doesBranchExist("http://repo", "main")).thenReturn(true);

        mockMvc.perform(get("/participations/github/branch-exists")
                        .param("repoUrl", "http://repo")
                        .param("branchName", "main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }
}
