package tn.esprit.matching.dto;

import tn.esprit.matching.entity.Invitation;

import java.time.LocalDateTime;

public class AdminInvitationDTO {

    private Long id;
    private Long projectId;
    private Long freelancerId;
    private String status;       // PENDING / ACCEPTED / DECLINED / TRASH
    private LocalDateTime createdAt;
    private LocalDateTime trashedAt;

    public AdminInvitationDTO() {}

    public AdminInvitationDTO(Invitation inv) {
        this.id = inv.getId();
        this.projectId = inv.getProjectId();
        this.freelancerId = inv.getFreelancerId();
        this.status = inv.getStatus().name();
        this.createdAt = inv.getCreatedAt();
        this.trashedAt = inv.getTrashedAt();
    }

    // getters/setters...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTrashedAt() {
        return trashedAt;
    }

    public void setTrashedAt(LocalDateTime trashedAt) {
        this.trashedAt = trashedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
    }}

