package tn.esprit.matching.clients;

import java.util.List;

public class ExperienceMatchingResponse {

    private List<ExperienceDTO> experiences;
    private Double totalYears;

    // ✅ OBLIGATOIRE pour Jackson/Feign
    public ExperienceMatchingResponse() {}

    public ExperienceMatchingResponse(List<ExperienceDTO> experiences, Double totalYears) {
        this.experiences = experiences;
        this.totalYears = totalYears;
    }

    public List<ExperienceDTO> getExperiences() { return experiences; }
    public void setExperiences(List<ExperienceDTO> experiences) { this.experiences = experiences; }

    public Double getTotalYears() { return totalYears; }
    public void setTotalYears(Double totalYears) { this.totalYears = totalYears; }
}