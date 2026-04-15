package tn.esprit.matching.service;

import org.junit.jupiter.api.Test;
import tn.esprit.matching.clients.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreServiceTest {

    private final ScoreService scoreService = new ScoreService();

    // ---------- scoreExperience ----------

    @Test
    void scoreExperience_whenExperienceNull_shouldReturn0() {
        double score = scoreService.scoreExperience(null, null);
        assertThat(score).isEqualTo(0);
    }

    @Test
    void scoreExperience_whenRatioGreaterOrEqual1_shouldReturn100() {
        ExperienceMatchingResponse exp = new ExperienceMatchingResponse();
        exp.setTotalYears(10.0);

        ProjectAnalysisDTO analysis = new ProjectAnalysisDTO();
        analysis.setComplexityScore(4); // requiredYears = 8.0 → ratio > 1

        double score = scoreService.scoreExperience(exp, analysis);
        assertThat(score).isEqualTo(100);
    }

    @Test
    void scoreExperience_whenRatioBetween0_8And1_shouldReturn85() {
        ExperienceMatchingResponse exp = new ExperienceMatchingResponse();
        exp.setTotalYears(8.0);

        ProjectAnalysisDTO analysis = new ProjectAnalysisDTO();
        analysis.setComplexityScore(10); // requiredYears = 20.0 → ratio = 0.4 -> 55, on va ajuster

        // corrigeons pour ratio ~ 0.8 :
        analysis.setComplexityScore(5); // requiredYears = 10 → ratio = 0.8
        double score = scoreService.scoreExperience(exp, analysis);
        assertThat(score).isEqualTo(85);
    }

    // ---------- scoreAvailability ----------

    @Test
    void scoreAvailability_whenNull_shouldReturn0() {
        double score = scoreService.scoreAvailability(null);
        assertThat(score).isEqualTo(0);
    }

    @Test
    void scoreAvailability_whenFull_shouldReturn100() {
        AvailabilityDTO availability = new AvailabilityDTO();
        availability.setStatus("FULL_TIME");

        double score = scoreService.scoreAvailability(availability);
        assertThat(score).isEqualTo(100);
    }

    @Test
    void scoreAvailability_whenPartTime10Hours_shouldBeBetween60And100() {
        AvailabilityDTO availability = new AvailabilityDTO();
        availability.setStatus("PART_TIME");
        availability.setHoursPerWeek(15);

        double score = scoreService.scoreAvailability(availability);
        assertThat(score).isGreaterThanOrEqualTo(60);
        assertThat(score).isLessThanOrEqualTo(100);
    }

    // ---------- scoreEducation ----------

    @Test
    void scoreEducation_whenNull_shouldReturn0() {
        double score = scoreService.scoreEducation(null, null);
        assertThat(score).isEqualTo(0);
    }

    @Test
    void scoreEducation_whenMasterAndFieldMatchesProjectCategory_shouldBeHigh() {
        EducationMatchingResponse edu = new EducationMatchingResponse();
        edu.setDegree("MASTER");
        edu.setFieldOfStudy("Computer Science");

        ProjectSkillDTO projectSkill = new ProjectSkillDTO();
        projectSkill.setCategory("computer"); // sera contenu dans "computer science"

        double score = scoreService.scoreEducation(edu, List.of(projectSkill));
        assertThat(score).isGreaterThanOrEqualTo(90); // base 90 + bonus
    }

    // ---------- scoreSkills ----------

    @Test
    void scoreSkills_whenNoUserSkillsOrProjectSkills_shouldReturn0() {
        double score = scoreService.scoreSkills(null, null);
        assertThat(score).isEqualTo(0);

        score = scoreService.scoreSkills(List.of(), List.of());
        assertThat(score).isEqualTo(0);
    }

    @Test
    void scoreSkills_whenExactMatch_shouldBeHigh() {
        FreelancerSkillMatchingResponse userSkill = new FreelancerSkillMatchingResponse();
        userSkill.setSkillName("Java");
        userSkill.setCategory("Backend");

        ProjectSkillDTO projectSkill = new ProjectSkillDTO();
        projectSkill.setSkillName("Java");
        projectSkill.setDemand("HIGH");
        projectSkill.setCategory("Backend");

        double score = scoreService.scoreSkills(List.of(userSkill), List.of(projectSkill));

        // comme calculateSkillMatch renvoie 100 pour un nom identique,
        // le score final doit être 100
        assertThat(score).isEqualTo(100);
    }

    @Test
    void scoreSkills_whenCategoryMatchButNameDifferent_shouldBeLower() {
        FreelancerSkillMatchingResponse userSkill = new FreelancerSkillMatchingResponse();
        userSkill.setSkillName("Spring Boot");
        userSkill.setCategory("Backend");

        ProjectSkillDTO projectSkill = new ProjectSkillDTO();
        projectSkill.setSkillName("Java");
        projectSkill.setDemand("MEDIUM");
        projectSkill.setCategory("Backend");

        double score = scoreService.scoreSkills(List.of(userSkill), List.of(projectSkill));

        // category match => 40 selon calculateSkillMatch
        assertThat(score).isEqualTo(40);
    }

    // ---------- calculateFinalScore ----------

    @Test
    void calculateFinalScore_shouldApplyWeights() {
        double finalScore = scoreService.calculateFinalScore(
                100, // skills
                80,  // experience
                60,  // education
                40   // availability
        );

        // = 100*0.50 + 80*0.25 + 60*0.15 + 40*0.10
        double expected = (100 * 0.50) + (80 * 0.25) + (60 * 0.15) + (40 * 0.10);

        assertThat(finalScore).isEqualTo(expected);
    }
}