package tn.freelancy.skillmanagement.dto;

import tn.freelancy.skillmanagement.entity.Skill;

public class SkillMatchResult {

    private Skill skill;
    private double confidence;
    private boolean exactMatch;
    private boolean suggestion;

    public SkillMatchResult() {}


    public SkillMatchResult(Skill skill, double confidence, boolean exactMatch, boolean suggestion) {
        this.skill = skill;
        this.confidence = confidence;
        this.exactMatch = exactMatch;
        this.suggestion = suggestion;
    }

    public Skill getSkill() {
        return skill;
    }

    public double getConfidence() {
        return confidence;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public boolean isSuggestion() {
        return suggestion;
    }

    public void setSkill(Skill s)        { this.skill = s; }
    public void setConfidence(double c)  { this.confidence = c; }
    public void setExactMatch(boolean e) { this.exactMatch = e; }
    public void setSuggestion(boolean s) { this.suggestion = s; }
}