package tn.esprit.planningg.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.planningg.entities.Planning;
import tn.esprit.planningg.services.IPlanningService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plannings")
@CrossOrigin(origins = "http://localhost:4200")
public class PlanningController {

    private final IPlanningService planningService;

    public PlanningController(IPlanningService planningService) {
        this.planningService = planningService;
    }

    @PostMapping
    public Planning addPlanning(@RequestBody Planning planning) {
        return planningService.addPlanning(planning);
    }

    @GetMapping
    public List<Planning> getAllPlannings() {
        return planningService.getAllPlannings();
    }

    @GetMapping("/{id}")
    public Planning getPlanning(@PathVariable Long id) {
        return planningService.getPlanningById(id);
    }

    @GetMapping("/{id}/analysis")
    public Map<String, Object> getPlanningAnalysis(@PathVariable Long id) {
        return planningService.getPlanningAnalysis(id);
    }

    @PutMapping("/{id}")
    public Planning updatePlanning(@PathVariable Long id, @RequestBody Planning planning) {
        planning.setId(id);
        return planningService.updatePlanning(planning);
    }

    @DeleteMapping("/{id}")
    public void deletePlanning(@PathVariable Long id) {
        planningService.deletePlanning(id);
    }
}
