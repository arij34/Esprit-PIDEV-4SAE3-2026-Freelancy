package tn.esprit.matching.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ClientInvitationDTO {

    private Long id;
    private Long projectId;
    private String projectTitle;
    private String freelancerName;   // Nom complet
    private String status;
    private String invitedAt;
    private String respondedAt;


    public ClientInvitationDTO() {}

    // Getters & Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getFreelancerName() {
        return freelancerName;
    }

    public void setFreelancerName(String freelancerName) {
        this.freelancerName = freelancerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInvitedAt() {
        return invitedAt;
    }

    public void setInvitedAt(String invitedAt) {
        this.invitedAt = invitedAt;
    }

    public String getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(String respondedAt) {
        this.respondedAt = respondedAt;
    }
}