package tn.esprit.matching.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitations")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "match_score")
    private Double matchScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    @Column(name = "trashed_at")
    private LocalDateTime trashedAt;

    String projectTitle;
    String freelancerFullName;

    public LocalDateTime getTrashedAt() { return trashedAt; }
    public void setTrashedAt(LocalDateTime trashedAt) { this.trashedAt = trashedAt; }

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }
    public String getFreelancerFullName() { return freelancerFullName; }
    public void setFreelancerFullName(String freelancerFullName) {
        this.freelancerFullName = freelancerFullName;
    }
    public void setId(long l) {
        this.id = l;
    }

    public void setCreatedAt(LocalDateTime of) {
        this.createdAt = of;
    }
}