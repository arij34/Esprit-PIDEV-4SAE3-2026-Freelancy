package tn.esprit.challengeservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.challengeservice.entities.ChallengeParticipation;
import tn.esprit.challengeservice.entities.ParticipationStatus;
import tn.esprit.challengeservice.entities.SonarCloudResult;
import tn.esprit.challengeservice.repositories.SonarCloudResultRepository;
import tn.esprit.challengeservice.repositories.participationRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineStatusSchedulerTest {

    @Mock
    private participationRepository participationRepository;

    @Mock
    private SonarCloudResultRepository sonarCloudResultRepository;

    @Mock
    private GitHubService gitHubService;

    @InjectMocks
    private PipelineStatusScheduler scheduler;

    @Test
    void checkSonarCloudPipelineStatus_whenNoSubmitted_shouldDoNothing() {
        when(participationRepository.findByStatus(ParticipationStatus.SUBMITTED)).thenReturn(List.of());

        scheduler.checkSonarCloudPipelineStatus();

        verify(sonarCloudResultRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void checkSonarCloudPipelineStatus_whenResultAlreadyExists_shouldSkip() {
        ChallengeParticipation participation = ChallengeParticipation.builder().id("p-1").repoName("repo-1").build();
        when(participationRepository.findByStatus(ParticipationStatus.SUBMITTED)).thenReturn(List.of(participation));
        when(sonarCloudResultRepository.findByParticipationId("p-1")).thenReturn(Optional.of(new SonarCloudResult()));

        scheduler.checkSonarCloudPipelineStatus();

        verify(gitHubService, never()).getLatestPullRequestNumber("repo-1");
        verify(sonarCloudResultRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void checkSonarCloudPipelineStatus_whenMetricsAvailable_shouldSaveResult() {
        ChallengeParticipation participation = ChallengeParticipation.builder().id("p-1").repoName("repo-1").build();
        when(participationRepository.findByStatus(ParticipationStatus.SUBMITTED)).thenReturn(List.of(participation));
        when(sonarCloudResultRepository.findByParticipationId("p-1")).thenReturn(Optional.empty());
        when(gitHubService.getLatestPullRequestNumber("repo-1")).thenReturn("12");
        when(gitHubService.fetchSonarCloudMetrics("repo-1", "12")).thenReturn(Map.of(
                "alert_status", "OK",
                "bugs", "1",
                "code_smells", "2",
                "vulnerabilities", "3",
                "security_hotspots", "4",
                "coverage", "88.5",
                "duplicated_lines_density", "1.1",
                "ncloc", "250"
        ));

        scheduler.checkSonarCloudPipelineStatus();

        ArgumentCaptor<SonarCloudResult> captor = ArgumentCaptor.forClass(SonarCloudResult.class);
        verify(sonarCloudResultRepository).save(captor.capture());
        SonarCloudResult saved = captor.getValue();

        assertEquals("OK", saved.getQualityGateStatus());
        assertEquals(1, saved.getBugs());
        assertEquals("12", saved.getPullRequestKey());
        assertNotNull(saved.getAnalyzedAt());
        assertEquals(participation, saved.getParticipation());
    }

    @Test
    void checkSonarCloudPipelineStatus_whenFetchFails_shouldContinueWithoutSaving() {
        ChallengeParticipation participation = ChallengeParticipation.builder().id("p-1").repoName("repo-1").build();
        when(participationRepository.findByStatus(ParticipationStatus.SUBMITTED)).thenReturn(List.of(participation));
        when(sonarCloudResultRepository.findByParticipationId("p-1")).thenReturn(Optional.empty());
        when(gitHubService.getLatestPullRequestNumber("repo-1")).thenThrow(new RuntimeException("not ready"));

        scheduler.checkSonarCloudPipelineStatus();

        verify(sonarCloudResultRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
