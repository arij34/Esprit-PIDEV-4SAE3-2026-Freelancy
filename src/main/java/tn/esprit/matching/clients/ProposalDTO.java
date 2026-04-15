package tn.esprit.matching.clients;

import java.time.LocalDate;

public class ProposalDTO {

    private Long id;
    private Long freelancerId;
    private String status;
    private LocalDate deadline;

    public ProposalDTO() {}

    public Long getId() { return id; }
    public Long getFreelancerId() { return freelancerId; }
    public String getStatus() { return status; }
    public LocalDate getDeadline() { return deadline; }
}