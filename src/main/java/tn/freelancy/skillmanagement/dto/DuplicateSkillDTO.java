package tn.freelancy.skillmanagement.dto;

public class DuplicateSkillDTO {

    private String skillA;
    private String skillB;
    private double confidence;

    public DuplicateSkillDTO(String skillA, String skillB, double confidence) {
        this.skillA = skillA;
        this.skillB = skillB;
        this.confidence = confidence;
    }

    public String getSkillA() { return skillA; }
    public String getSkillB() { return skillB; }
    public double getConfidence() { return confidence; }
}
