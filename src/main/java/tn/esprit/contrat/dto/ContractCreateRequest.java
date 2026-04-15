package tn.esprit.contrat.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ContractCreateRequest {

    private String    title;
    private String    description;
    private Long      projectId;
    private Long      proposalId;
    private Long      freelancerId;
    private Long      clientId;
    private BigDecimal totalAmount;
    private String    currency = "TND";
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate deadline;
    private List<MilestoneRequest> milestones;

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public String getTitle()                        { return title; }
    public void setTitle(String title)              { this.title = title; }

    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }

    public Long getProjectId()                      { return projectId; }
    public void setProjectId(Long projectId)        { this.projectId = projectId; }

    public Long getProposalId()                     { return proposalId; }
    public void setProposalId(Long proposalId)      { this.proposalId = proposalId; }

    public Long getFreelancerId()                   { return freelancerId; }
    public void setFreelancerId(Long freelancerId)  { this.freelancerId = freelancerId; }

    public Long getClientId()                       { return clientId; }
    public void setClientId(Long clientId)          { this.clientId = clientId; }

    public BigDecimal getTotalAmount()              { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency()                     { return currency; }
    public void setCurrency(String currency)        { this.currency = currency; }

    public LocalDate getStartDate()                 { return startDate; }
    public void setStartDate(LocalDate startDate)   { this.startDate = startDate; }

    public LocalDate getEndDate()                   { return endDate; }
    public void setEndDate(LocalDate endDate)       { this.endDate = endDate; }

    public LocalDate getDeadline()                  { return deadline; }
    public void setDeadline(LocalDate deadline)     { this.deadline = deadline; }

    public List<MilestoneRequest> getMilestones()   { return milestones; }
    public void setMilestones(List<MilestoneRequest> milestones) { this.milestones = milestones; }
}