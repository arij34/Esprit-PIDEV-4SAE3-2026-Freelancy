package tn.esprit.matching.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_responses")
public class ApplicationResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invitation_id", nullable = false)
    private Long invitationId;

    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "answer_q1", columnDefinition = "TEXT")
    private String answerQ1;

    @Column(name = "answer_q2", columnDefinition = "TEXT")
    private String answerQ2;

    @Column(name = "answer_q3")
    private String answerQ3;

    @Column(name = "answer_q4")
    private String answerQ4;

    @Column(name = "answer_q5", columnDefinition = "TEXT")
    private String answerQ5;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
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

    public String getAnswerQ2() {
        return answerQ2;
    }

    public void setAnswerQ2(String answerQ2) {
        this.answerQ2 = answerQ2;
    }

    public String getAnswerQ1() {
        return answerQ1;
    }

    public void setAnswerQ1(String answerQ1) {
        this.answerQ1 = answerQ1;
    }

    public String getAnswerQ3() {
        return answerQ3;
    }

    public void setAnswerQ3(String answerQ3) {
        this.answerQ3 = answerQ3;
    }

    public String getAnswerQ5() {
        return answerQ5;
    }

    public void setAnswerQ5(String answerQ5) {
        this.answerQ5 = answerQ5;
    }

    public String getAnswerQ4() {
        return answerQ4;
    }

    public void setAnswerQ4(String answerQ4) {
        this.answerQ4 = answerQ4;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}