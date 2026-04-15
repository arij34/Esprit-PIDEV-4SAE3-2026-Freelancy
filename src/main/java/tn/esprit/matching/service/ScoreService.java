package tn.esprit.matching.service;

import org.springframework.stereotype.Service;
import tn.esprit.matching.clients.*;

import java.util.List;

@Service
public class ScoreService {

    private static final double WEIGHT_SKILLS = 0.50;
    private static final double WEIGHT_EXP    = 0.25;
    private static final double WEIGHT_EDU    = 0.15;
    private static final double WEIGHT_AVAIL  = 0.10;

    /**
     * Score d'expérience basé sur le nombre total d'années
     * et la complexité du projet (complexityScore de l'analyse).
     */
    public double scoreExperience(ExperienceMatchingResponse experience, ProjectAnalysisDTO analysis) {
        if (experience == null || experience.getTotalYears() == null) {
            return 0;
        }

        double userYears = experience.getTotalYears();
        double requiredYears = (analysis != null && analysis.getComplexityScore() != null)
                ? analysis.getComplexityScore() * 2.0
                : 5.0;

        double ratio = userYears / requiredYears;

        if (ratio >= 1.0) return 100;
        if (ratio >= 0.8) return 85;
        if (ratio >= 0.6) return 70;
        if (ratio >= 0.4) return 55;

        return 40;
    }

    /**
     * Score de disponibilité :
     * - FULL → 100
     * - PART_TIME → base 60 + bonus en fonction des heures dispo
     */
    public double scoreAvailability(AvailabilityDTO availability) {
        if (availability == null || availability.getStatus() == null) {
            return 0;
        }

        String status = availability.getStatus().toUpperCase();
        double score = status.contains("FULL") ? 100 : 60;

        if (status.contains("PART") && availability.getHoursPerWeek() != null) {
            if (availability.getHoursPerWeek() >= 20)      score += 10;
            else if (availability.getHoursPerWeek() >= 10) score += 5;
        }

        return Math.min(score, 100);
    }

    /**
     * Score d'éducation basé sur le diplôme + bonus si le domaine
     * correspond aux catégories des skills projet.
     */
    public double scoreEducation(EducationMatchingResponse education, List<ProjectSkillDTO> projectSkills) {
        if (education == null) {
            return 0;
        }

        String degree = education.getDegree() != null ? education.getDegree().toUpperCase() : "";
        String field  = education.getFieldOfStudy() != null ? education.getFieldOfStudy().toLowerCase() : "";

        double baseScore = switch (degree) {
            case "PHD", "DOCTORATE"          -> 100;
            case "ENGINEER", "MASTER", "MBA" -> 90;
            case "BACHELOR"                  -> 75;
            default                          -> 50;
        };

        boolean fieldMatch = projectSkills != null && projectSkills.stream()
                .anyMatch(s -> s.getCategory() != null
                        && !field.isEmpty()
                        && field.contains(s.getCategory().toLowerCase()));

        double bonus = fieldMatch ? 20 : 0;
        return Math.min(baseScore + bonus, 100);
    }

    /**
     * Score de skills basé sur la correspondance texte
     * et la demande (HIGH/MEDIUM/LOW) du skill côté projet.
     */
    public double scoreSkills(List<FreelancerSkillMatchingResponse> userSkills,
                              List<ProjectSkillDTO> projectSkills) {
        if (userSkills == null || projectSkills == null || projectSkills.isEmpty()) {
            return 0;
        }

        double totalScore  = 0;
        double totalWeight = 0;

        for (ProjectSkillDTO ps : projectSkills) {

            String demand = ps.getDemand() != null ? ps.getDemand().toUpperCase() : "MEDIUM";
            double weight = switch (demand) {
                case "HIGH" -> 3.0;
                case "LOW"  -> 1.0;
                default     -> 2.0;
            };
            totalWeight += weight;

            double matchFactor = userSkills.stream()
                    .mapToDouble(us -> calculateSkillMatch(us, ps))
                    .max()
                    .orElse(0.0);

            totalScore += (matchFactor * weight);
        }

        return totalWeight == 0 ? 0 : (totalScore / totalWeight);
    }

    private double calculateSkillMatch(FreelancerSkillMatchingResponse us, ProjectSkillDTO ps) {

        String uName     = us.getSkillName() != null ? us.getSkillName().toLowerCase().trim()  : "";
        String pName     = ps.getSkillName() != null ? ps.getSkillName().toLowerCase().trim()  : "";
        String uCategory = us.getCategory()  != null ? us.getCategory().toLowerCase().trim()   : "";
        String pCategory = ps.getCategory()  != null ? ps.getCategory().toLowerCase().trim()   : "";

        if (uName.isEmpty() || pName.isEmpty()) return 0;

        if (uName.equals(pName))                            return 100;
        if (uName.contains(pName) || pName.contains(uName)) return 70;
        if (!uCategory.isEmpty() && uCategory.equals(pCategory)) return 40;

        return 0;
    }

    /**
     * Combinaison pondérée des scores partiels.
     */
    public double calculateFinalScore(double skillsScore,
                                      double experienceScore,
                                      double educationScore,
                                      double availabilityScore) {
        return (skillsScore      * WEIGHT_SKILLS)
                + (experienceScore  * WEIGHT_EXP)
                + (educationScore   * WEIGHT_EDU)
                + (availabilityScore* WEIGHT_AVAIL);
    }
}