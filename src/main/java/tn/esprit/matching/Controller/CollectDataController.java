package tn.esprit.matching.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import tn.esprit.matching.clients.*;
import tn.esprit.matching.service.CollectDataService;

@RestController
@RequestMapping("/collectdata")
public class CollectDataController {

    @Autowired
    private CollectDataService matchingService;

    @GetMapping("/experience/{userId}")
    public ExperienceMatchingResponse test(@PathVariable Long userId) {
        return matchingService.getExperience(userId); // ✅ corrigé
    }

    @GetMapping("/availability/{userId}")
    public AvailabilityDTO testAvailability(@PathVariable Long userId) {
        return matchingService.getAvailability(userId);
    }

    @GetMapping("/education/{userId}")
    public EducationMatchingResponse testEducation(@PathVariable Long userId) {
        return matchingService.getEducation(userId);
    }

    @GetMapping("/skills/{userId}")
    public List<FreelancerSkillMatchingResponse> testSkills(@PathVariable Long userId) {
        return matchingService.getFreelancerSkills(userId);
    }

    @GetMapping("/project/{projectId}")
    public ProjectDTO testProject(@PathVariable Long projectId) {
        return matchingService.getProject(projectId);
    }

    @GetMapping("/project/{projectId}/skills")
    public List<ProjectSkillDTO> testProjectSkills(@PathVariable Long projectId) {
        return matchingService.getProjectSkills(projectId);
    }

    @GetMapping("/project/{projectId}/analysis")
    public ProjectAnalysisDTO testProjectAnalysis(@PathVariable Long projectId) {
        return matchingService.getProjectAnalysis(projectId);
    }
}