package tn.freelancy.skillmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class FreelancerSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private level level;

    private Integer yearsExperience;

    private Boolean extractedByAI;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)  // ← EAGER au lieu de LAZY
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "custom_skill_name")
    private String customSkillName;

    public FreelancerSkill(Integer yearsExperience, Boolean extractedByAI, tn.freelancy.skillmanagement.entity.level level,String customSkillName) {
        this.yearsExperience = yearsExperience;
        this.extractedByAI = extractedByAI;
        this.level = level;
        this.customSkillName = customSkillName;
    }

    public FreelancerSkill() {
    }





    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public tn.freelancy.skillmanagement.entity.level getLevel() {
        return level;
    }

    public void setLevel(tn.freelancy.skillmanagement.entity.level level) {
        this.level = level;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Integer yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public Boolean getExtractedByAI() {
        return extractedByAI;
    }

    public void setExtractedByAI(Boolean extractedByAI) {
        this.extractedByAI = extractedByAI;
    }





    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }
    public String getCustomSkillName() {
        return customSkillName;
    }

    public void setCustomSkillName(String customSkillName) {
        this.customSkillName = customSkillName;
    }
}
