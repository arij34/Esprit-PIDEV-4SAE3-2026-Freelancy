package tn.esprit.matching.dto;

public class FreelancerMatchedProjectDTO {

    private Long projectId;
    private double matchScore; // en %, par ex. 0–100 ou 0–1, à toi de choisir

    public FreelancerMatchedProjectDTO() {}

    public FreelancerMatchedProjectDTO(Long projectId, double matchScore) {
        this.projectId = projectId;
        this.matchScore = matchScore;
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }


}