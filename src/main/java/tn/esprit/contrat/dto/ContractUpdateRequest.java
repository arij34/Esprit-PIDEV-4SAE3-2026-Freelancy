package tn.esprit.contrat.dto;

import java.math.BigDecimal;
import java.util.List;

public class ContractUpdateRequest {

    private String     title;
    private String     description;
    private BigDecimal totalAmount;
    private String     currency;
    private String     startDate;   // ✅ String
    private String     endDate;     // ✅ String
    private String     deadline;    // ✅ String
    private List<MilestoneRequest> milestones; // ✅ Ajouté

    public String     getTitle()                              { return title; }
    public void       setTitle(String title)                  { this.title = title; }

    public String     getDescription()                        { return description; }
    public void       setDescription(String description)      { this.description = description; }

    public BigDecimal getTotalAmount()                        { return totalAmount; }
    public void       setTotalAmount(BigDecimal a)            { this.totalAmount = a; }

    public String     getCurrency()                           { return currency; }
    public void       setCurrency(String currency)            { this.currency = currency; }

    public String     getStartDate()                          { return startDate; }
    public void       setStartDate(String startDate)          { this.startDate = startDate; }

    public String     getEndDate()                            { return endDate; }
    public void       setEndDate(String endDate)              { this.endDate = endDate; }

    public String     getDeadline()                           { return deadline; }
    public void       setDeadline(String deadline)            { this.deadline = deadline; }

    public List<MilestoneRequest> getMilestones()             { return milestones; }
    public void setMilestones(List<MilestoneRequest> m)       { this.milestones = m; }
}