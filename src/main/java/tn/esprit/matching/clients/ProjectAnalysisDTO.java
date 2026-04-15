package tn.esprit.matching.clients;



public class ProjectAnalysisDTO {

    private String complexityLevel;
    private Integer complexityScore;

    private String freelancersAvailability;
    private Integer freelancersEstimatedCount;

    private String riskLevel;
    private Integer riskScore;

    // getters & setters

    public String getComplexityLevel() {
        return complexityLevel;
    }

    public void setComplexityLevel(String complexityLevel) {
        this.complexityLevel = complexityLevel;
    }

    public Integer getComplexityScore() {
        return complexityScore;
    }

    public void setComplexityScore(Integer complexityScore) {
        this.complexityScore = complexityScore;
    }

    public String getFreelancersAvailability() {
        return freelancersAvailability;
    }

    public void setFreelancersAvailability(String freelancersAvailability) {
        this.freelancersAvailability = freelancersAvailability;
    }

    public Integer getFreelancersEstimatedCount() {
        return freelancersEstimatedCount;
    }

    public void setFreelancersEstimatedCount(Integer freelancersEstimatedCount) {
        this.freelancersEstimatedCount = freelancersEstimatedCount;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }
}
