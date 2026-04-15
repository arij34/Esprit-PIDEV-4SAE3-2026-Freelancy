package tn.esprit.matching.dto;

public class MatchingResultDTO {

    private Long userId;
    private Long projectId;
    private double skillsScore;
    private double experienceScore;
    private double educationScore;
    private double availabilityScore;
    private double finalScore;

    // Constructeur vide
    public MatchingResultDTO() {}

    // Constructeur complet
    public MatchingResultDTO(Long userId, Long projectId,
                             double skillsScore, double experienceScore,
                             double educationScore, double availabilityScore,
                             double finalScore) {
        this.userId            = userId;
        this.projectId         = projectId;
        this.skillsScore       = skillsScore;
        this.experienceScore   = experienceScore;
        this.educationScore    = educationScore;
        this.availabilityScore = availabilityScore;
        this.finalScore        = finalScore;
    }

    // Getters
    public Long getUserId()               { return userId; }
    public Long getProjectId()            { return projectId; }
    public double getSkillsScore()        { return skillsScore; }
    public double getExperienceScore()    { return experienceScore; }
    public double getEducationScore()     { return educationScore; }
    public double getAvailabilityScore()  { return availabilityScore; }
    public double getFinalScore()         { return finalScore; }

    // Setters
    public void setUserId(Long userId)                       { this.userId = userId; }
    public void setProjectId(Long projectId)                 { this.projectId = projectId; }
    public void setSkillsScore(double skillsScore)           { this.skillsScore = skillsScore; }
    public void setExperienceScore(double experienceScore)   { this.experienceScore = experienceScore; }
    public void setEducationScore(double educationScore)     { this.educationScore = educationScore; }
    public void setAvailabilityScore(double availabilityScore){ this.availabilityScore = availabilityScore; }
    public void setFinalScore(double finalScore)             { this.finalScore = finalScore; }
}