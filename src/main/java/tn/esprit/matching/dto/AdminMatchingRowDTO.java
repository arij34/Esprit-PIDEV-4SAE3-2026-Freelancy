package tn.esprit.matching.dto;

import tn.esprit.matching.entity.Matching;

public class AdminMatchingRowDTO {

    private Long id;
    private Long projectId;
    private Long freelancerId;

    private double scoreSkills;
    private double scoreExperience;
    private double scoreEducation;
    private double scoreAvailability;
    private double scoreFinal;

    private String status; // CALCULATED / ...

    public AdminMatchingRowDTO() {}

    public AdminMatchingRowDTO(Matching m) {
        this.id = m.getId();
        this.projectId = m.getProjectId();
        this.freelancerId = m.getFreelancerId();
        this.scoreSkills = m.getScoreSkills();
        this.scoreExperience = m.getScoreExperience();
        this.scoreEducation = m.getScoreEducation();
        this.scoreAvailability = m.getScoreAvailability();
        this.scoreFinal = m.getScoreFinal();
        this.status = m.getStatus();
    }

    // getters/setters...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public double getScoreSkills() {
        return scoreSkills;
    }

    public void setScoreSkills(double scoreSkills) {
        this.scoreSkills = scoreSkills;
    }

    public double getScoreExperience() {
        return scoreExperience;
    }

    public void setScoreExperience(double scoreExperience) {
        this.scoreExperience = scoreExperience;
    }

    public double getScoreEducation() {
        return scoreEducation;
    }

    public void setScoreEducation(double scoreEducation) {
        this.scoreEducation = scoreEducation;
    }

    public double getScoreAvailability() {
        return scoreAvailability;
    }

    public void setScoreAvailability(double scoreAvailability) {
        this.scoreAvailability = scoreAvailability;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getScoreFinal() {
        return scoreFinal;
    }

    public void setScoreFinal(double scoreFinal) {
        this.scoreFinal = scoreFinal;
    }
}