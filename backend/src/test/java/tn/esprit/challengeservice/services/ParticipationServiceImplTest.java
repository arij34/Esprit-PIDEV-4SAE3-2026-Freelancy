package tn.esprit.challengeservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.challengeservice.clients.UserDto;
import tn.esprit.challengeservice.clients.UserServiceClient;
import tn.esprit.challengeservice.entities.*;
import tn.esprit.challengeservice.exceptions.SonarResultsNotFoundException;
import tn.esprit.challengeservice.repositories.ChallengeRepository;
import tn.esprit.challengeservice.repositories.SonarCloudResultRepository;
import tn.esprit.challengeservice.repositories.participationRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceImplTest {

    @Mock
    private participationRepository participationRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private GitHubService gitHubService;

    @Mock
    private SonarCloudResultRepository sonarCloudResultRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ParticipationServiceImpl participationService;

    private Challenge challenge;
    private ChallengeParticipation participation;
    private UserDto currentUser;

    @BeforeEach
    void setUp() {
        challenge = Challenge.builder()
                .idChallenge("ch-001")
                .title("Docker Challenge")
                .status(ChallengeStatus.ACTIVE)
                .points(100L)
                .build();

        participation = ChallengeParticipation.builder()
                .id("p-001")
                .userId(42L)
                .usernameGithub("ameni-dev")
                .repoUrl("https://github.com/challenge-org/Docker-Challenge-ameni-dev")
                .repoName("Docker-Challenge-ameni-dev")
                .forkCreatedAt(new Date())
                .status(ParticipationStatus.ACTIVE)
                .challenge(challenge)
                .build();

        currentUser = new UserDto();
        currentUser.setId(42L);
    }

    // ─── joinChallenge ────────────────────────────────────────────────────────

    @Test
    void joinChallenge_whenValidRequest_shouldCreateParticipation() {
        when(userServiceClient.getCurrentUser("Bearer token")).thenReturn(currentUser);
        when(challengeRepository.findById("ch-001")).thenReturn(Optional.of(challenge));
        when(participationRepository.existsByChallengeIdChallengeAndUserId("ch-001", 42L))
                .thenReturn(false);
        when(gitHubService.createRepository(any())).thenReturn("https://github.com/org/repo");
        doNothing().when(gitHubService).addCollaborator(any(), any());
        when(participationRepository.save(any())).thenReturn(participation);

        ChallengeParticipation result = participationService.joinChallenge(
                "ch-001", "ameni-dev", "Bearer token");

        assertNotNull(result);
        assertEquals(ParticipationStatus.ACTIVE, result.getStatus());
        verify(participationRepository).save(any());
    }

    @Test
    void joinChallenge_whenChallengeNotActive_shouldThrowRuntimeException() {
        challenge.setStatus(ChallengeStatus.CLOSED);
        when(userServiceClient.getCurrentUser("Bearer token")).thenReturn(currentUser);
        when(challengeRepository.findById("ch-001")).thenReturn(Optional.of(challenge));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> participationService.joinChallenge("ch-001", "ameni-dev", "Bearer token"));

        assertTrue(ex.getMessage().contains("not active"));
    }

    @Test
    void joinChallenge_whenAlreadyJoined_shouldThrowRuntimeException() {
        when(userServiceClient.getCurrentUser("Bearer token")).thenReturn(currentUser);
        when(challengeRepository.findById("ch-001")).thenReturn(Optional.of(challenge));
        when(participationRepository.existsByChallengeIdChallengeAndUserId("ch-001", 42L))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> participationService.joinChallenge("ch-001", "ameni-dev", "Bearer token"));

        assertTrue(ex.getMessage().contains("already joined"));
    }

    @Test
    void joinChallenge_whenChallengeNotFound_shouldThrowRuntimeException() {
        when(userServiceClient.getCurrentUser("Bearer token")).thenReturn(currentUser);
        when(challengeRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> participationService.joinChallenge("bad-id", "ameni-dev", "Bearer token"));
    }

    // ─── getAllParticipations ─────────────────────────────────────────────────

    @Test
    void getAllParticipations_shouldReturnList() {
        when(participationRepository.findAll()).thenReturn(List.of(participation));

        List<ChallengeParticipation> result = participationService.getAllParticipations();

        assertEquals(1, result.size());
        verify(participationRepository).findAll();
    }

    // ─── getParticipationById ─────────────────────────────────────────────────

    @Test
    void getParticipationById_whenExists_shouldReturn() {
        when(participationRepository.findById("p-001")).thenReturn(Optional.of(participation));

        ChallengeParticipation result = participationService.getParticipationById("p-001");

        assertNotNull(result);
        assertEquals("p-001", result.getId());
    }

    @Test
    void getParticipationById_whenNotFound_shouldThrow() {
        when(participationRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> participationService.getParticipationById("bad-id"));
    }

    // ─── getParticipationsByChallenge ─────────────────────────────────────────

    @Test
    void getParticipationsByChallenge_shouldReturnList() {
        when(participationRepository.findByChallengeIdChallenge("ch-001"))
                .thenReturn(List.of(participation));

        List<ChallengeParticipation> result =
                participationService.getParticipationsByChallenge("ch-001");

        assertEquals(1, result.size());
    }

    // ─── deleteParticipation ──────────────────────────────────────────────────

    @Test
    void deleteParticipation_whenExists_shouldDelete() {
        when(participationRepository.existsById("p-001")).thenReturn(true);
        doNothing().when(participationRepository).deleteById("p-001");

        assertDoesNotThrow(() -> participationService.deleteParticipation("p-001"));
        verify(participationRepository).deleteById("p-001");
    }

    @Test
    void deleteParticipation_whenNotFound_shouldThrow() {
        when(participationRepository.existsById("bad-id")).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> participationService.deleteParticipation("bad-id"));
        verify(participationRepository, never()).deleteById(any());
    }

    // ─── getTotalParticipantsCount ────────────────────────────────────────────

    @Test
    void getTotalParticipantsCount_shouldReturnCount() {
        when(participationRepository.count()).thenReturn(10L);

        long count = participationService.getTotalParticipantsCount();

        assertEquals(10L, count);
    }

    // ─── submitChallenge ──────────────────────────────────────────────────────

    @Test
    void submitChallenge_whenActiveParticipation_shouldReturnPrUrl() {
        when(participationRepository.findById("p-001")).thenReturn(Optional.of(participation));
        doNothing().when(gitHubService).ensureSonarSetupOrThrow(any());
        when(gitHubService.createPullRequest(any(), any()))
                .thenReturn("https://github.com/org/repo/pull/1");
        when(participationRepository.save(any())).thenReturn(participation);

        String prUrl = participationService.submitChallenge("p-001", "solution-branch");

        assertNotNull(prUrl);
        assertTrue(prUrl.contains("pull"));
        verify(participationRepository).save(any());
    }

    @Test
    void submitChallenge_whenNotActive_shouldThrow() {
        participation.setStatus(ParticipationStatus.SUBMITTED);
        when(participationRepository.findById("p-001")).thenReturn(Optional.of(participation));

        assertThrows(RuntimeException.class,
                () -> participationService.submitChallenge("p-001", "branch"));
    }

    // ─── getSonarResults ──────────────────────────────────────────────────────

    @Test
    void getSonarResults_whenExists_shouldReturn() {
        SonarCloudResult sonarResult = new SonarCloudResult();
        sonarResult.setQualityGateStatus("OK");
        sonarResult.setBugs(0);

        when(sonarCloudResultRepository.findByParticipationId("p-001"))
                .thenReturn(Optional.of(sonarResult));

        SonarCloudResult result = participationService.getSonarResults("p-001");

        assertNotNull(result);
        assertEquals("OK", result.getQualityGateStatus());
    }

    @Test
    void getSonarResults_whenNotFound_shouldThrowSonarResultsNotFoundException() {
        when(sonarCloudResultRepository.findByParticipationId("p-001"))
                .thenReturn(Optional.empty());

        assertThrows(SonarResultsNotFoundException.class,
                () -> participationService.getSonarResults("p-001"));
    }

    // ─── saveSonarPoints ──────────────────────────────────────────────────────

    @Test
    void saveSonarPoints_whenValid_shouldSave() {
        SonarCloudResult sonarResult = new SonarCloudResult();
        sonarResult.setId("sr-001");
        sonarResult.setParticipation(participation);
        participation.getChallenge().setPoints(100L);

        when(sonarCloudResultRepository.findById("sr-001"))
                .thenReturn(Optional.of(sonarResult));
        when(sonarCloudResultRepository.save(any())).thenReturn(sonarResult);

        assertDoesNotThrow(() ->
                participationService.saveSonarPoints("p-001", "sr-001", 80));
        verify(sonarCloudResultRepository).save(sonarResult);
    }

    @Test
    void saveSonarPoints_whenNegativePoints_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> participationService.saveSonarPoints("p-001", "sr-001", -5));
    }

    @Test
    void saveSonarPoints_whenExceedsMaxPoints_shouldThrowIllegalArgumentException() {
        SonarCloudResult sonarResult = new SonarCloudResult();
        sonarResult.setId("sr-001");
        sonarResult.setParticipation(participation);
        participation.getChallenge().setPoints(100L);

        when(sonarCloudResultRepository.findById("sr-001"))
                .thenReturn(Optional.of(sonarResult));

        assertThrows(IllegalArgumentException.class,
                () -> participationService.saveSonarPoints("p-001", "sr-001", 150));
    }
}
