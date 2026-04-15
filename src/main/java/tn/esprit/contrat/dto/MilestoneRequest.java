package tn.esprit.contrat.dto;

import java.math.BigDecimal;

public class MilestoneRequest {

    private String     title;
    private String     description;
    private BigDecimal amount;
    private String     deadline;  // ✅ String, pas LocalDate
    private Integer    orderIndex;

    public String     getTitle()                          { return title; }
    public void       setTitle(String title)              { 
        this.title = title;
        System.out.println("  DTO setTitle: " + title);
    }

    public String     getDescription()                    { return description; }
    public void       setDescription(String description)  { 
        this.description = description;
        System.out.println("  DTO setDescription: " + description);
    }

    public BigDecimal getAmount()                         { return amount; }
    public void       setAmount(BigDecimal amount)        { 
        this.amount = amount;
        System.out.println("  DTO setAmount: " + amount);
    }

    public String     getDeadline()                       { return deadline; }
    public void       setDeadline(String deadline)        { 
        this.deadline = deadline;
        System.out.println("  DTO setDeadline: " + deadline);
    }

    public Integer    getOrderIndex()                     { return orderIndex; }
    public void       setOrderIndex(Integer orderIndex)   { 
        this.orderIndex = orderIndex;
        System.out.println("  DTO setOrderIndex: " + orderIndex);
    }

    @Override
    public String toString() {
        return "MilestoneRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", deadline='" + deadline + '\'' +
                ", orderIndex=" + orderIndex +
                '}';
    }
}