package tn.esprit.planningg.services;

import org.springframework.stereotype.Service;
import tn.esprit.planningg.entities.Planning;
import tn.esprit.planningg.entities.Priorite;
import tn.esprit.planningg.entities.Statut;
import tn.esprit.planningg.entities.Task;
import tn.esprit.planningg.repositories.PlanningRepository;
import tn.esprit.planningg.repositories.TaskRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PlanningServiceImpl implements IPlanningService {

    private final PlanningRepository planningRepository;
    private final TaskRepository taskRepository;

    public PlanningServiceImpl(PlanningRepository planningRepository, TaskRepository taskRepository) {
        this.planningRepository = planningRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public Planning addPlanning(Planning planning) {
        return planningRepository.save(planning);
    }

    @Override
    public Planning updatePlanning(Planning planning) {
        return planningRepository.save(planning);
    }

    @Override
    public Planning getPlanningById(Long id) {
        return planningRepository.findById(id).orElse(null);
    }

    @Override
    public List<Planning> getAllPlannings() {
        return StreamSupport.stream(planningRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPlanningAnalysis(Long id) {
        Planning planning = planningRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planning introuvable avec id=" + id));

        List<Task> tasks = taskRepository.findByPlanning_Id(id);
        LocalDateTime now = LocalDateTime.now();

        long overdueCount = tasks.stream()
                .filter(t -> t.getDateFin() != null && t.getDateFin().isBefore(now) && t.getStatut() != Statut.DONE)
                .count();

        long criticalCount = tasks.stream()
                .filter(t -> t.getPriorite() == Priorite.CRITICAL || t.getPriorite() == Priorite.HIGH)
                .count();

        long doneCount = tasks.stream().filter(t -> t.getStatut() == Statut.DONE).count();
        double completionRate = tasks.isEmpty() ? 0.0 : (doneCount * 100.0) / tasks.size();

        Map<String, Long> byStatus = tasks.stream()
                .collect(Collectors.groupingBy(t -> String.valueOf(t.getStatut()), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> byPriority = tasks.stream()
                .collect(Collectors.groupingBy(t -> String.valueOf(t.getPriorite()), LinkedHashMap::new, Collectors.counting()));

        String focus;
        if (overdueCount > 0) {
            focus = "Traiter immediatement les taches en retard";
        } else if (criticalCount > 0) {
            focus = "Prioriser les taches critiques et a haute priorite";
        } else {
            focus = "Planning stable, maintenir le rythme actuel";
        }

        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("planningId", planning.getId());
        analysis.put("planningType", planning.getType());
        analysis.put("totalTasks", tasks.size());
        analysis.put("doneTasks", doneCount);
        analysis.put("overdueTasks", overdueCount);
        analysis.put("criticalTasks", criticalCount);
        analysis.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        analysis.put("tasksByStatus", byStatus);
        analysis.put("tasksByPriority", byPriority);
        analysis.put("recommendedFocus", focus);
        return analysis;
    }

    @Override
    public void deletePlanning(Long id) {
        planningRepository.deleteById(id);
    }
}
