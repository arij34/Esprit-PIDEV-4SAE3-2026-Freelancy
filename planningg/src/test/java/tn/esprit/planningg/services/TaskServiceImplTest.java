package tn.esprit.planningg.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import tn.esprit.planningg.dto.AiTaskSuggestionRequest;
import tn.esprit.planningg.entities.Planning;
import tn.esprit.planningg.entities.Statut;
import tn.esprit.planningg.entities.Task;
import tn.esprit.planningg.repositories.PlanningRepository;
import tn.esprit.planningg.repositories.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PlanningRepository planningRepository;

    @Mock
    private JavaMailSender mailSender;

    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(
                taskRepository,
                planningRepository,
                mailSender,
                "to@example.com",
                "from@example.com",
                "",
                "gemini-2.5-flash",
                "https://generativelanguage.googleapis.com/v1beta/models"
        );
    }

    @Test
    void addTask_shouldSaveTaskAndSendMail() {
        Planning planning = new Planning();
        planning.setId(1L);

        Task task = new Task();
        task.setTitre("Prep");
        task.setDateDebut(LocalDateTime.now().plusHours(1));
        task.setDateFin(LocalDateTime.now().plusHours(4));
        task.setPlanning(planning);
        task.setStatut(Statut.TODO);

        when(planningRepository.findById(1L)).thenReturn(Optional.of(planning));
        when(taskRepository.save(task)).thenReturn(task);

        Task saved = taskService.addTask(task);

        assertNotNull(saved);
        assertEquals("Prep", saved.getTitre());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void updateTask_shouldSaveTask() {
        Planning planning = new Planning();
        planning.setId(2L);

        Task task = new Task();
        task.setId(5L);
        task.setDateDebut(LocalDateTime.now().plusHours(2));
        task.setDateFin(LocalDateTime.now().plusHours(3));
        task.setPlanning(planning);
        task.setStatut(Statut.IN_PROGRESS);

        when(planningRepository.findById(2L)).thenReturn(Optional.of(planning));
        when(taskRepository.save(task)).thenReturn(task);

        Task updated = taskService.updateTask(task);

        assertNotNull(updated);
        assertEquals(5L, updated.getId());
    }

    @Test
    void getTaskById_shouldReturnTask() {
        Task task = new Task();
        task.setId(9L);

        when(taskRepository.findById(9L)).thenReturn(Optional.of(task));

        Task found = taskService.getTaskById(9L);

        assertNotNull(found);
        assertEquals(9L, found.getId());
    }

    @Test
    void getAllTasks_shouldReturnAll() {
        Task task = new Task();
        task.setId(11L);

        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(1, tasks.size());
        assertEquals(11L, tasks.get(0).getId());
    }

    @Test
    void deleteTask_shouldCallRepository() {
        taskService.deleteTask(7L);

        verify(taskRepository).deleteById(7L);
    }

    @Test
    void getOverdueTasks_shouldReturnSortedList() {
        Task older = new Task();
        older.setDateFin(LocalDateTime.now().minusDays(2));
        older.setStatut(Statut.TODO);

        Task recent = new Task();
        recent.setDateFin(LocalDateTime.now().minusHours(2));
        recent.setStatut(Statut.IN_PROGRESS);

        when(taskRepository.findByDateFinBeforeAndStatutNot(any(LocalDateTime.class),
                eq(Statut.DONE))).thenReturn(List.of(recent, older));

        List<Task> overdue = taskService.getOverdueTasks();

        assertEquals(2, overdue.size());
        assertEquals(older.getDateFin(), overdue.get(0).getDateFin());
    }

    @Test
    void addTask_shouldThrowWhenDatesInvalid() {
        Task task = new Task();
        task.setDateDebut(LocalDateTime.now().plusDays(2));
        task.setDateFin(LocalDateTime.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> taskService.addTask(task));
    }

    @Test
    void generateTaskSuggestions_shouldThrowWhenRequestMissingPlanningId() {
        AiTaskSuggestionRequest request = new AiTaskSuggestionRequest();

        assertThrows(IllegalArgumentException.class, () -> taskService.generateTaskSuggestions(request));
    }

    @Test
    void generateTaskSuggestions_shouldThrowWhenApiKeyMissing() {
        AiTaskSuggestionRequest request = new AiTaskSuggestionRequest();
        request.setPlanningId(1L);
        request.setTargetCount(3);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskService.generateTaskSuggestions(request)
        );

        assertNotNull(exception.getMessage());
    }
}
