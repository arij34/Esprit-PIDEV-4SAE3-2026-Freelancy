package tn.esprit.challengeservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.challengeservice.entities.*;
import tn.esprit.challengeservice.repositories.ChallengeRepository;
import tn.esprit.challengeservice.repositories.TaskRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Challenge challenge;
    private Task task;

    @BeforeEach
    void setUp() {
        challenge = Challenge.builder()
                .idChallenge("ch-001")
                .title("Docker Challenge")
                .status(ChallengeStatus.ACTIVE)
                .build();

        task = Task.builder()
                .idTask("t-001")
                .title("Setup Docker")
                .description("Configure Dockerfile")
                .status(TaskStatus.ACTIVE)
                .deadline(new Date())
                .challenge(challenge)
                .build();
    }

    // ─── addTask ─────────────────────────────────────────────────────────────

    @Test
    void addTask_whenChallengeExists_shouldSaveAndReturn() {
        when(challengeRepository.findById("ch-001")).thenReturn(Optional.of(challenge));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.addTask("ch-001", task);

        assertNotNull(result);
        assertEquals("Setup Docker", result.getTitle());
        assertEquals(challenge, result.getChallenge());
        verify(taskRepository).save(task);
    }

    @Test
    void addTask_whenChallengeNotFound_shouldThrowRuntimeException() {
        when(challengeRepository.findById("bad-id")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.addTask("bad-id", task));

        assertTrue(ex.getMessage().contains("Challenge not found"));
        verify(taskRepository, never()).save(any());
    }

    // ─── getTasksByChallenge ──────────────────────────────────────────────────

    @Test
    void getTasksByChallenge_shouldReturnTaskList() {
        Task task2 = Task.builder()
                .idTask("t-002")
                .title("Write Tests")
                .status(TaskStatus.INPROGRESS)
                .challenge(challenge)
                .build();

        when(taskRepository.findByChallengeIdChallenge("ch-001"))
                .thenReturn(List.of(task, task2));

        List<Task> result = taskService.getTasksByChallenge("ch-001");

        assertEquals(2, result.size());
        verify(taskRepository).findByChallengeIdChallenge("ch-001");
    }

    @Test
    void getTasksByChallenge_whenNoTasks_shouldReturnEmptyList() {
        when(taskRepository.findByChallengeIdChallenge("ch-001"))
                .thenReturn(List.of());

        List<Task> result = taskService.getTasksByChallenge("ch-001");

        assertTrue(result.isEmpty());
    }

    // ─── updateTask ───────────────────────────────────────────────────────────

    @Test
    void updateTask_whenExists_shouldUpdateFields() {
        Task updatedData = Task.builder()
                .title("Updated Title")
                .description("Updated Description")
                .status(TaskStatus.COMPLETED)
                .deadline(new Date())
                .build();

        when(taskRepository.findById("t-001")).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.updateTask("t-001", updatedData);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_whenNotFound_shouldThrowRuntimeException() {
        when(taskRepository.findById("bad-id")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTask("bad-id", task));

        assertTrue(ex.getMessage().contains("Task not found"));
    }

    // ─── updateTaskStatus ─────────────────────────────────────────────────────

    @Test
    void updateTaskStatus_whenExists_shouldUpdateStatus() {
        when(taskRepository.findById("t-001")).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.updateTaskStatus("t-001", TaskStatus.COMPLETED);

        assertNotNull(result);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_whenNotFound_shouldThrowRuntimeException() {
        when(taskRepository.findById("bad-id")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTaskStatus("bad-id", TaskStatus.COMPLETED));

        assertTrue(ex.getMessage().contains("bad-id"));
    }

    // ─── deleteTask ───────────────────────────────────────────────────────────

    @Test
    void deleteTask_shouldCallRepositoryDelete() {
        doNothing().when(taskRepository).deleteById("t-001");

        assertDoesNotThrow(() -> taskService.deleteTask("t-001"));
        verify(taskRepository).deleteById("t-001");
    }
}
