package tn.esprit.matching.service;

import org.springframework.stereotype.Service;
import tn.esprit.matching.clients.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CollectDataService {

    private final SkillClient skillClient;
    private final ProjectClient projectClient;

    public CollectDataService(SkillClient skillClient, ProjectClient projectClient) {
        this.skillClient = skillClient;
        this.projectClient = projectClient;
    }

    // --- MÉTHODES INDIVIDUELLES ---

    public ExperienceMatchingResponse getExperience(Long userId) {
        return skillClient.getExperiences(userId);
    }

    public AvailabilityDTO getAvailability(Long userId) {
        return skillClient.getAvailability(userId);
    }

    public EducationMatchingResponse getEducation(Long userId) {
        return skillClient.getEducation(userId);
    }

    public List<FreelancerSkillMatchingResponse> getFreelancerSkills(Long userId) {
        return skillClient.getFreelancerSkills(userId);
    }

    public ProjectDTO getProject(Long projectId) {
        return projectClient.getProjectById(projectId);
    }

    public List<ProjectSkillDTO> getProjectSkills(Long projectId) {
        return projectClient.getProjectSkills(projectId);
    }

    public ProjectAnalysisDTO getProjectAnalysis(Long projectId) {
        return projectClient.getProjectAnalysis(projectId);
    }

    // --- ASYNC MATCHING ---

    public CompletableFuture<MatchingDataPackage> getAllData(Long userId, Long projectId) {

        var expFuture    = CompletableFuture.supplyAsync(() -> skillClient.getExperiences(userId));
        var availFuture  = CompletableFuture.supplyAsync(() -> skillClient.getAvailability(userId));
        var eduFuture    = CompletableFuture.supplyAsync(() -> skillClient.getEducation(userId));
        var skillFuture  = CompletableFuture.supplyAsync(() -> skillClient.getFreelancerSkills(userId));
        var projFuture   = CompletableFuture.supplyAsync(() -> projectClient.getProjectById(projectId));
        var pSkillFuture = CompletableFuture.supplyAsync(() -> projectClient.getProjectSkills(projectId));
        var analFuture   = CompletableFuture.supplyAsync(() -> projectClient.getProjectAnalysis(projectId));

        return CompletableFuture.allOf(
                expFuture, availFuture, eduFuture, skillFuture, projFuture, pSkillFuture, analFuture
        ).thenApply(v -> new MatchingDataPackage(
                expFuture.join(),
                availFuture.join(),
                eduFuture.join(),
                skillFuture.join(),
                projFuture.join(),
                pSkillFuture.join(),
                analFuture.join()
        ));
    }

    public record MatchingDataPackage(
            ExperienceMatchingResponse experience,
            AvailabilityDTO availability,
            EducationMatchingResponse education,
            List<FreelancerSkillMatchingResponse> userSkills,
            ProjectDTO project,
            List<ProjectSkillDTO> projectSkills,
            ProjectAnalysisDTO analysis
    ) {}
}