package tn.esprit.planningg.dto;

public class AiTaskSuggestionRequest {
    private Long planningId;
    private Integer targetCount;

    public Long getPlanningId() {
        return planningId;
    }

    public void setPlanningId(Long planningId) {
        this.planningId = planningId;
    }

    public Integer getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(Integer targetCount) {
        this.targetCount = targetCount;
    }
}
