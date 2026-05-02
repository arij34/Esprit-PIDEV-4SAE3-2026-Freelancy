package tn.esprit.challengeservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.challengeservice.entities.*;
import tn.esprit.challengeservice.repositories.ChallengeRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceImplTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private ChallengeServiceImpl challengeService;

    private Challenge challenge;

    @BeforeEach
    void setUp() {
        challenge = Challenge.builder()
                .idChallenge("ch-001")
                .title("Docker Challenge")
                .description("Build a Docker pipeline")
                .category("DevOps")
                .technology("Docker,Kubernetes")
                .difficulty(ChallengeDifficulty.INTERMEDIATE)
                .status(ChallengeStatus.ACTIVE)
                .points(100L)
                .maxParticipants(50)
                .build();
    }

    // ─── addChallenge ────────────────────────────────────────────────────────

    @Test
    void addChallenge_withNoTasks_shouldSaveAndReturn() {
        challenge.setTasks(null);
        when(challengeRepository.save(challenge)).thenReturn(challenge);

        Challenge result = challengeService.addChallenge(challenge);

        assertNotNull(result);
        assertEquals("Docker Challenge", result.getTitle());
        verify(challengeRepository, times(1)).save(challenge);
    }

    @Test
    void addChallenge_withTasks_shouldLinkTasksToChallenge() {
        Task task1 = Task.builder().idTask("t-1").title("Task 1").build();
        Task task2 = Task.builder().idTask("t-2").title("Task 2").build();
        challenge.setTasks(List.of(task1, task2));

        when(challengeRepository.save(challenge)).thenReturn(challenge);

        Challenge result = challengeService.addChallenge(challenge);

        assertNotNull(result);
        result.getTasks().forEach(task ->
                assertEquals(challenge, task.getChallenge())
        );
        verify(challengeRepository).save(challenge);
    }

    // ─── getChallengeById ────────────────────────────────────────────────────

    @Test
    void getChallengeById_whenExists_shouldReturnChallenge() {
        when(challengeRepository.findById("ch-001")).thenReturn(Optional.of(challenge));

        Challenge result = challengeService.getChallengeById("ch-001");

        assertNotNull(result);
        assertEquals("ch-001", result.getIdChallenge());
    }

    @Test
    void getChallengeById_whenNotFound_shouldThrowRuntimeException() {
        when(challengeRepository.findById("invalid-id")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> challengeService.getChallengeById("invalid-id"));

        assertTrue(ex.getMessage().contains("invalid-id"));
    }

    // ─── getAllChallenges ─────────────────────────────────────────────────────

    @Test
    void getAllChallenges_shouldReturnAllChallenges() {
        Challenge challenge2 = Challenge.builder()
                .idChallenge("ch-002")
                .title("React Challenge")
                .status(ChallengeStatus.ACTIVE)
                .build();

        when(challengeRepository.findAll()).thenReturn(List.of(challenge, challenge2));

        List<Challenge> result = challengeService.getAllChallenges();

        assertEquals(2, result.size());
        verify(challengeRepository).findAll();
    }

    @Test
    void getAllChallenges_whenEmpty_shouldReturnEmptyList() {
        when(challengeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Challenge> result = challengeService.getAllChallenges();

        assertTrue(result.isEmpty());
    }

    // ─── updateChallenge ─────────────────────────────────────────────────────

    @Test
    void updateChallenge_whenExists_shouldUpdateAndReturn() {
        Challenge updatedData = Challenge.builder()
                .title("Updated Title")
                .category("Web Development")
                .technology("React")
                .difficulty(ChallengeDifficulty.ADVANCED)
                .description("Updated description")
                .points(200L)
                .maxParticipants(100)
                .status(ChallengeStatus.COMPLETED)
                .build();

        when(challengeRepository.findById("ch-001")).thenReturn(Optional.of(challenge));
        when(challengeRepository.save(any(Challenge.class))).thenReturn(challenge);

        Challenge result = challengeService.updateChallenge("ch-001", updatedData);

        assertNotNull(result);
        verify(challengeRepository).save(any(Challenge.class));
    }

    @Test
    void updateChallenge_whenNotFound_shouldThrowRuntimeException() {
        when(challengeRepository.findById("bad-id")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> challengeService.updateChallenge("bad-id", challenge));
    }

    // ─── deleteChallenge ─────────────────────────────────────────────────────

    @Test
    void deleteChallenge_whenExists_shouldDelete() {
        when(challengeRepository.existsById("ch-001")).thenReturn(true);
        doNothing().when(challengeRepository).deleteById("ch-001");

        assertDoesNotThrow(() -> challengeService.deleteChallenge("ch-001"));
        verify(challengeRepository).deleteById("ch-001");
    }

    @Test
    void deleteChallenge_whenNotFound_shouldThrowRuntimeException() {
        when(challengeRepository.existsById("bad-id")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> challengeService.deleteChallenge("bad-id"));

        assertTrue(ex.getMessage().contains("bad-id"));
        verify(challengeRepository, never()).deleteById(any());
    }

    // ─── countActiveChallenges ───────────────────────────────────────────────

    @Test
    void countActiveChallenges_shouldReturnCorrectCount() {
        when(challengeRepository.countByStatus(ChallengeStatus.ACTIVE)).thenReturn(5L);

        long count = challengeService.countActiveChallenges();

        assertEquals(5L, count);
    }

    // ─── countCompletedChallenges ────────────────────────────────────────────

    @Test
    void countCompletedChallenges_shouldReturnCorrectCount() {
        when(challengeRepository.countByStatus(ChallengeStatus.COMPLETED)).thenReturn(3L);

        long count = challengeService.countCompletedChallenges();

        assertEquals(3L, count);
    }

    // ─── getTechnologyCounts ─────────────────────────────────────────────────

    @Test
    void getTechnologyCounts_shouldCountKnownTechnologies() {
        Challenge c1 = Challenge.builder().technology("Docker,Kubernetes").build();
        Challenge c2 = Challenge.builder().technology("Docker,React").build();

        when(challengeRepository.findAll()).thenReturn(List.of(c1, c2));

        Map<String, Long> result = challengeService.getTechnologyCounts();

        assertNotNull(result);
        assertEquals(2L, result.get("Docker"));
        assertEquals(1L, result.get("Kubernetes"));
        assertEquals(1L, result.get("React"));
    }

    @Test
    void getTechnologyCounts_whenNullTechnology_shouldSkip() {
        Challenge c1 = Challenge.builder().technology(null).build();
        Challenge c2 = Challenge.builder().technology("  ").build();

        when(challengeRepository.findAll()).thenReturn(List.of(c1, c2));

        Map<String, Long> result = challengeService.getTechnologyCounts();

        assertNotNull(result);
        result.values().forEach(count -> assertEquals(0L, count));
    }

    // ─── getCategoryCounts ───────────────────────────────────────────────────

    @Test
    void getCategoryCounts_shouldCountKnownCategories() {
        Challenge c1 = Challenge.builder().category("DevOps").build();
        Challenge c2 = Challenge.builder().category("DevOps").build();
        Challenge c3 = Challenge.builder().category("Web Development").build();

        when(challengeRepository.findAll()).thenReturn(List.of(c1, c2, c3));

        Map<String, Long> result = challengeService.getCategoryCounts();

        assertNotNull(result);
        assertEquals(2L, result.get("DevOps"));
        assertEquals(1L, result.get("Web Development"));
    }

    @Test
    void getCategoryCounts_whenNullCategory_shouldSkip() {
        Challenge c1 = Challenge.builder().category(null).build();

        when(challengeRepository.findAll()).thenReturn(List.of(c1));

        Map<String, Long> result = challengeService.getCategoryCounts();

        assertNotNull(result);
        result.values().forEach(count -> assertEquals(0L, count));
    }
}
