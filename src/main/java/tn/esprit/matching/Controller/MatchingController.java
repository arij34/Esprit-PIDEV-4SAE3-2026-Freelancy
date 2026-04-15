package tn.esprit.matching.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.matching.dto.FreelancerMatchDTO;
import tn.esprit.matching.dto.FreelancerMatchedProjectDTO;
import tn.esprit.matching.dto.MatchingResultDTO;
import tn.esprit.matching.service.*;

import java.util.List;

@RestController
@RequestMapping("/matching")
public class MatchingController {

    @Autowired
    private CollectDataService collectDataService;

    @Autowired
    private MatchingService matchingService;

    @Autowired
    private ScoreService scoreService;

    @GetMapping("/score/full/{userId}/{projectId}")
    public MatchingResultDTO getFullMatching(@PathVariable Long userId,
                                             @PathVariable Long projectId) {

        var data = collectDataService.getAllData(userId, projectId).join();

        double availabilityScore = scoreService.scoreAvailability(data.availability());
        double educationScore    = scoreService.scoreEducation(data.education(), data.projectSkills());
        double skillsScore       = scoreService.scoreSkills(data.userSkills(), data.projectSkills());
        double experienceScore   = scoreService.scoreExperience(data.experience(), data.analysis());

        double finalScore = scoreService.calculateFinalScore(
                skillsScore, experienceScore, educationScore, availabilityScore
        );

        return new MatchingResultDTO(
                userId,
                projectId,
                skillsScore,
                experienceScore,
                educationScore,
                availabilityScore,
                finalScore
        );
    }

    // ✅ CORRIGÉ : ajouter token
    @PostMapping("/project/{projectId}")
    public List<FreelancerMatchDTO> getMatchingForProject(
            @PathVariable Long projectId,
            @RequestBody List<Long> freelancerIds,
            @RequestHeader("Authorization") String token
    ) {
        return matchingService.getMatchingForProject(projectId, freelancerIds, token);
    }

    @GetMapping("/{projectId}")
    public List<FreelancerMatchDTO> getMatchingByProject(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String token
    ) {
        return matchingService.getMatchingForProjectAuto(projectId, token);
    }

    @GetMapping("/freelancer/{freelancerId}/project-scores")
    public ResponseEntity<List<FreelancerMatchedProjectDTO>> getMatchedProjectScores(
            @PathVariable Long freelancerId) {

        List<FreelancerMatchedProjectDTO> list =
                matchingService.getMatchedProjectIdsForFreelancer(freelancerId);
        return ResponseEntity.ok(list);
    }
}