package tn.esprit.matching.clients;

public class SkillDTO {
        private Long SkillId;
        private String name;
        private String normalizedName;
        private String category;

    public Long getSkillId() {
        return SkillId;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}


