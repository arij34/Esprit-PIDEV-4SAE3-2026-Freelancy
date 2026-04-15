package tn.esprit.matching.dto;

import java.time.LocalDateTime;
import java.util.List;

public class InvitationDTO {

    private Long id;
    private Long projectId;

    private String projectTitle;
    private String projectDescription;
    private String clientName;   // Nom complet
    private String clientEmail;  // Email

    private Double matchScore;
    private String status;
    private String invitedAt;
    private String deadline;

    // Budget
    private Integer budgetMin;
    private Integer budgetMax;
    private Integer budgetRecommended;

    // Duration
    private Integer durationEstimatedWeeks;

    private List<String> requiredSkills;

    private LocalDateTime trashedAt;
    private String freelancerFullName; // "FirstName LastName"



    public InvitationDTO() {}

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String projectDescription) { this.projectDescription = projectDescription; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getInvitedAt() { return invitedAt; }
    public void setInvitedAt(String invitedAt) { this.invitedAt = invitedAt; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public Integer getBudgetMin() { return budgetMin; }
    public void setBudgetMin(Integer budgetMin) { this.budgetMin = budgetMin; }

    public Integer getBudgetMax() { return budgetMax; }
    public void setBudgetMax(Integer budgetMax) { this.budgetMax = budgetMax; }

    public Integer getBudgetRecommended() { return budgetRecommended; }
    public void setBudgetRecommended(Integer budgetRecommended) { this.budgetRecommended = budgetRecommended; }

    public Integer getDurationEstimatedWeeks() { return durationEstimatedWeeks; }
    public void setDurationEstimatedWeeks(Integer durationEstimatedWeeks) { this.durationEstimatedWeeks = durationEstimatedWeeks; }

    public List<String> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }

    public LocalDateTime getTrashedAt() { return trashedAt; }
    public void setTrashedAt(LocalDateTime trashedAt) { this.trashedAt = trashedAt; }

    // + getter/setter
    public String getFreelancerFullName() { return freelancerFullName; }
    public void setFreelancerFullName(String freelancerFullName) { this.freelancerFullName = freelancerFullName; }
}