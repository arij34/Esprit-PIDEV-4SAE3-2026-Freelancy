package tn.esprit.planningg.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.planningg.entities.Planning;
import tn.esprit.planningg.entities.Priorite;
import tn.esprit.planningg.entities.Statut;
import tn.esprit.planningg.entities.Task;
import tn.esprit.planningg.repositories.PlanningRepository;
import tn.esprit.planningg.repositories.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanningServiceImplTest {

    @Mock
    private PlanningRepository planningRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private PlanningServiceImpl planningService;

    @Test
    void getPlanningAnalysis_shouldComputeMetrics_whenPlanningExists() {
        Long planningId = 1L;

        Planning planning = new Planning();
        planning.setId(planningId);
        planning.setType("Sprint");

        Task overdue = new Task();
        overdue.setDateFin(LocalDateTime.now().minusDays(1));
        overdue.setStatut(Statut.TODO);
        overdue.setPriorite(Priorite.CRITICAL);

        Task done = new Task();
        done.setDateFin(LocalDateTime.now().plusDays(1));
        done.setStatut(Statut.DONE);
        done.setPriorite(Priorite.LOW);

        Task high = new Task();
        high.setDateFin(LocalDateTime.now().plusHours(2));
        high.setStatut(Statut.IN_PROGRESS);
        high.setPriorite(Priorite.HIGH);

        when(planningRepository.findById(planningId)).thenReturn(Optional.of(planning));
        when(taskRepository.findByPlanning_Id(planningId)).thenReturn(List.of(overdue, done, high));

        Map<String, Object> analysis = planningService.getPlanningAnalysis(planningId);

        assertEquals(planningId, analysis.get("planningId"));
        assertEquals("Sprint", analysis.get("planningType"));
        assertEquals(3, analysis.get("totalTasks"));
        assertEquals(1L, analysis.get("doneTasks"));
        assertEquals(1L, analysis.get("overdueTasks"));
        assertEquals(2L, analysis.get("criticalTasks"));
        assertEquals(33.33, analysis.get("completionRate"));
        assertEquals("Traiter immediatement les taches en retard", analysis.get("recommendedFocus"));
    }

    @Test
    void getPlanningAnalysis_shouldThrow_whenPlanningMissing() {
        Long planningId = 99L;
        when(planningRepository.findById(planningId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planningService.getPlanningAnalysis(planningId)
        );

        assertEquals("Planning introuvable avec id=99", exception.getMessage());
    }
}
