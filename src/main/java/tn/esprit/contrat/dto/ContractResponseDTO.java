package tn.esprit.contrat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import tn.esprit.contrat.entity.ContractMilestone;
import tn.esprit.contrat.entity.ContractClause;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractResponseDTO {

    private Long   id;
    private String title;
    private String description;
    private Long   projectId;
    private Long   proposalId;
    private Long   freelancerId;
    private Long   clientId;

    // ─── Infos freelancer enrichies ──────────────────────────────────────────
    private String freelancerName;
    private String freelancerEmail;
    private String freelancerRole;

    // ─── Infos client enrichies ──────────────────────────────────────────────
    private String clientName;

    private BigDecimal totalAmount;
    private String     currency;
    private LocalDate  startDate;
    private LocalDate  endDate;
    private String     status;

    // ─── Signature info (freelancer / client) ───────────────────────────────
    private LocalDateTime freelancerSignedAt;
    private LocalDateTime clientSignedAt;

    // Lien vers le PDF signé (si les deux parties ont signé)
    private String pdfUrl;

    // Chemins (facultatifs) vers les images de signatures électroniques
    private String freelancerSignatureImagePath;
    private String clientSignatureImagePath;

    private List<ContractMilestone> milestones;
    private List<ContractClause>    clauses;
    private LocalDateTime createdAt;

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public Long getId()                             { return id; }
    public void setId(Long id)                      { this.id = id; }

    public String getTitle()                        { return title; }
    public void setTitle(String title)              { this.title = title; }

    public String getDescription()                  { return description; }

    public void setDescription(String description) {
        if (description == null) { this.description = ""; return; }
        String cleaned = description
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "")
                .replaceAll("[\uFFFD]", "")
                .trim();
        if (cleaned.length() > 2000) cleaned = cleaned.substring(0, 2000) + "...";
        this.description = cleaned;
    }

    public Long getProjectId()                      { return projectId; }
    public void setProjectId(Long projectId)        { this.projectId = projectId; }

    public Long getProposalId()                     { return proposalId; }
    public void setProposalId(Long proposalId)      { this.proposalId = proposalId; }

    public Long getFreelancerId()                   { return freelancerId; }
    public void setFreelancerId(Long freelancerId)  { this.freelancerId = freelancerId; }

    public Long getClientId()                       { return clientId; }
    public void setClientId(Long clientId)          { this.clientId = clientId; }

    public String getFreelancerName()               { return freelancerName; }
    public void setFreelancerName(String n)         { this.freelancerName = n; }

    public String getFreelancerEmail()              { return freelancerEmail; }
    public void setFreelancerEmail(String e)        { this.freelancerEmail = e; }

    public String getFreelancerRole()               { return freelancerRole; }
    public void setFreelancerRole(String r)         { this.freelancerRole = r; }

    public String getClientName()                   { return clientName; }
    public void setClientName(String n)             { this.clientName = n; }

    public BigDecimal getTotalAmount()              { return totalAmount; }
    public void setTotalAmount(BigDecimal a)        { this.totalAmount = a; }

    public String getCurrency()                     { return currency; }
    public void setCurrency(String currency)        { this.currency = currency; }

    public LocalDate getStartDate()                 { return startDate; }
    public void setStartDate(LocalDate startDate)   { this.startDate = startDate; }

    public LocalDate getEndDate()                   { return endDate; }
    public void setEndDate(LocalDate endDate)       { this.endDate = endDate; }

    public String getStatus()                       { return status; }
    public void setStatus(String status)            { this.status = status; }

    public LocalDateTime getFreelancerSignedAt()                { return freelancerSignedAt; }
    public void setFreelancerSignedAt(LocalDateTime freelancerSignedAt) { this.freelancerSignedAt = freelancerSignedAt; }

    public LocalDateTime getClientSignedAt()                    { return clientSignedAt; }
    public void setClientSignedAt(LocalDateTime clientSignedAt) { this.clientSignedAt = clientSignedAt; }

    public String getPdfUrl()                       { return pdfUrl; }
    public void setPdfUrl(String pdfUrl)            { this.pdfUrl = pdfUrl; }

    public String getFreelancerSignatureImagePath() { return freelancerSignatureImagePath; }
    public void setFreelancerSignatureImagePath(String path) { this.freelancerSignatureImagePath = path; }

    public String getClientSignatureImagePath()     { return clientSignatureImagePath; }
    public void setClientSignatureImagePath(String path) { this.clientSignatureImagePath = path; }

    public List<ContractMilestone> getMilestones()          { return milestones; }
    public void setMilestones(List<ContractMilestone> m)    { this.milestones = m; }

    public List<ContractClause> getClauses()                { return clauses; }
    public void setClauses(List<ContractClause> c)          { this.clauses = c; }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}