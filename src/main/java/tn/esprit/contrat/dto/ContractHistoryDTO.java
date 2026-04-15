package tn.esprit.contrat.dto;

import java.time.LocalDateTime;

public class ContractHistoryDTO {

    private Long id;
    private String action;
    private Long performedBy;
    private String oldValue;
    private String newValue;
    private String aiSummary;
    private LocalDateTime performedAt;

    public ContractHistoryDTO() {}

    public ContractHistoryDTO(Long id, String action, Long performedBy,
                              String oldValue, String newValue,
                              String aiSummary, LocalDateTime performedAt) {
        this.id = id;
        this.action = action;
        this.performedBy = performedBy;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.aiSummary = aiSummary;
        this.performedAt = performedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Long getPerformedBy() { return performedBy; }
    public void setPerformedBy(Long performedBy) { this.performedBy = performedBy; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
}
