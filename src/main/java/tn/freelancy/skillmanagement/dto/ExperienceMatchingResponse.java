package tn.freelancy.skillmanagement.dto;

import java.util.List;

public class ExperienceMatchingResponse {

    private List<ExperienceDTO> experiences;
    private Double totalYears;

    public ExperienceMatchingResponse(List<ExperienceDTO> experiences, Double totalYears) {
        this.experiences = experiences;
        this.totalYears = totalYears;
    }

    public List<ExperienceDTO> getExperiences() {
        return experiences;
    }

    public Double getTotalYears() {
        return totalYears;
    }
}