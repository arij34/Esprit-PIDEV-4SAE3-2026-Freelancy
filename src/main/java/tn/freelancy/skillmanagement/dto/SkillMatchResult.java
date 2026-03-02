package tn.freelancy.skillmanagement.dto;

import tn.freelancy.skillmanagement.entity.Skill;

public class SkillMatchResult {

    private Skill skill;
    private double confidence;
    private boolean exactMatch;
    private boolean suggestion;

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
}