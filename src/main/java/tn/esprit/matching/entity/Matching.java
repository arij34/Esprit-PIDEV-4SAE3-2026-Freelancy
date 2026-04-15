package tn.esprit.matching.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matching")
public class Matching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "freelancer_id")
    private Long freelancerId;

    @Column(name = "score_skills")
    private double scoreSkills;

    @Column(name = "score_availability")
    private double scoreAvailability;

    @Column(name = "score_experience")
    private double scoreExperience;

    @Column(name = "score_education")
    private double scoreEducation;

    @Column(name = "score_challenges")
    private double scoreChallenges;

    @Column(name = "score_final")
    private double scoreFinal;

    private String status = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ✅ Auto-remplir createdAt avant chaque insertion
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public double getScoreSkills() { return scoreSkills; }
    public void setScoreSkills(double scoreSkills) { this.scoreSkills = scoreSkills; }

    public double getScoreAvailability() { return scoreAvailability; }
    public void setScoreAvailability(double scoreAvailability) { this.scoreAvailability = scoreAvailability; }

    public double getScoreExperience() { return scoreExperience; }
    public void setScoreExperience(double scoreExperience) { this.scoreExperience = scoreExperience; }

    public double getScoreEducation() { return scoreEducation; }
    public void setScoreEducation(double scoreEducation) { this.scoreEducation = scoreEducation; }

    public double getScoreChallenges() { return scoreChallenges; }
    public void setScoreChallenges(double scoreChallenges) { this.scoreChallenges = scoreChallenges; }

    public double getScoreFinal() { return scoreFinal; }
    public void setScoreFinal(double scoreFinal) { this.scoreFinal = scoreFinal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}