package tn.esprit.matching.dto;

public class SendInvitationRequest {

    private Long projectId;
    private Long freelancerId;
    private Long clientId;
    private Double matchScore;

    public Long getProjectId() { return projectId; }
    public Long getFreelancerId() { return freelancerId; }
    public Long getClientId() { return clientId; }
    public Double getMatchScore() { return matchScore; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
}