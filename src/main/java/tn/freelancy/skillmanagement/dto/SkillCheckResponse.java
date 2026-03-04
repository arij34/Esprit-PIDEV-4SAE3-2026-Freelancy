package tn.freelancy.skillmanagement.dto;

import java.util.List;

public class SkillCheckResponse {

    private List<String> existing;
    private List<String> newSkills;

    public SkillCheckResponse() {}

    public SkillCheckResponse(List<String> existing, List<String> newSkills) {
        this.existing = existing;
        this.newSkills = newSkills;
    }

    public List<String> getExisting() {
        return existing;
    }

    public void setExisting(List<String> existing) {
        this.existing = existing;
    }

    public List<String> getNewSkills() {
        return newSkills;
    }

    public void setNewSkills(List<String> newSkills) {
        this.newSkills = newSkills;
    }
}