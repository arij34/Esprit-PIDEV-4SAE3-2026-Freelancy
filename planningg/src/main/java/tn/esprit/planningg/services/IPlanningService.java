package tn.esprit.planningg.services;
import tn.esprit.planningg.entities.Planning;

import java.util.List;
import java.util.Map;

public interface IPlanningService {
    Planning addPlanning(Planning planning);
    Planning updatePlanning(Planning planning);
    Planning getPlanningById(Long id);
    List<Planning> getAllPlannings();
    Map<String, Object> getPlanningAnalysis(Long id);
    void deletePlanning(Long id);
}
