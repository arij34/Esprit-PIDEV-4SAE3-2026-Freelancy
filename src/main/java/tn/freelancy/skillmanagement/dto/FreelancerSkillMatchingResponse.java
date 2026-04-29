package tn.freelancy.skillmanagement.dto;

import tn.freelancy.skillmanagement.entity.level;

public class FreelancerSkillMatchingResponse {

    private Long userId;

    private Long skillId;              // référence (peut être null)
    private String skillName;          // depuis table skill
    private String normalizedName;     // important pour matching
    private String category;

    private String customSkillName;    // si user a écrit son propre skill

    private level level;
    private Integer yearsExperience;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCustomSkillName() {
        return customSkillName;
    }

    public void setCustomSkillName(String customSkillName) {
        this.customSkillName = customSkillName;
    }

    public level getLevel() {
        return level;
    }

    public void setLevel(level level) {
        this.level = level;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Integer yearsExperience) {
        this.yearsExperience = yearsExperience;
    }
}