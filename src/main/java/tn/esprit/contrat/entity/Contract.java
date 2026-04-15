package tn.esprit.contrat.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({
        "hibernateLazyInitializer",
        "handler",
        "history",
        "dispute",
        "documents",
        "payments"
})
@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = true)   // FIX: nullable pour création manuelle sans proposal
    private Long proposalId;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long freelancerId;

    @Column(name = "freelancer_keycloak_id", nullable = false)
    private String freelancerKeycloakId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency = "TND";

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.DRAFT;

    private LocalDateTime clientSignedAt;
    private LocalDateTime freelancerSignedAt;

    // Chemins vers les images de signatures électroniques (PNG)
    private String freelancerSignatureImagePath;
    private String clientSignatureImagePath;

    // ─── DocuSign eSignature Fields ───────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private SignatureStatus signatureStatus;

    @Column(name = "docusign_envelope_id")
    private String envelopeId;

    @Column(name = "signing_url", columnDefinition = "TEXT")
    private String signingUrl;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "signed_document_url", columnDefinition = "TEXT")
    private String signedDocumentUrl;

    private String pdfUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Sécurité : tout nouveau contrat doit démarrer en DRAFT
        // Même si un statut a été mis à PENDING_SIGNATURE avant le persist,
        // on le ramène à DRAFT lors de la création.
        if (status == null || status == ContractStatus.PENDING_SIGNATURE) {
            status = ContractStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ─── RELATIONS CORRIGÉES ───────────────────────────────────────────────

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @JsonManagedReference
    private List<ContractMilestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("article ASC")
    private List<ContractClause> clauses = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<ContractHistory> history = new ArrayList<>();

    @OneToOne(mappedBy = "contract", cascade = CascadeType.ALL)
    private ContractDispute dispute;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<ContractDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<ContractPayment> payments = new ArrayList<>();

    // ─── GETTERS / SETTERS ───────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getProposalId() { return proposalId; }
    public void setProposalId(Long proposalId) { this.proposalId = proposalId; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public String getFreelancerKeycloakId() { return freelancerKeycloakId; }
    public void setFreelancerKeycloakId(String freelancerKeycloakId) { this.freelancerKeycloakId = freelancerKeycloakId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }

    public LocalDateTime getClientSignedAt() { return clientSignedAt; }
    public void setClientSignedAt(LocalDateTime clientSignedAt) { this.clientSignedAt = clientSignedAt; }

    public LocalDateTime getFreelancerSignedAt() { return freelancerSignedAt; }
    public void setFreelancerSignedAt(LocalDateTime freelancerSignedAt) { this.freelancerSignedAt = freelancerSignedAt; }

    public String getFreelancerSignatureImagePath() { return freelancerSignatureImagePath; }
    public void setFreelancerSignatureImagePath(String freelancerSignatureImagePath) { this.freelancerSignatureImagePath = freelancerSignatureImagePath; }

    public String getClientSignatureImagePath() { return clientSignatureImagePath; }
    public void setClientSignatureImagePath(String clientSignatureImagePath) { this.clientSignatureImagePath = clientSignatureImagePath; }

    public SignatureStatus getSignatureStatus() { return signatureStatus; }
    public void setSignatureStatus(SignatureStatus signatureStatus) { this.signatureStatus = signatureStatus; }

    public String getEnvelopeId() { return envelopeId; }
    public void setEnvelopeId(String envelopeId) { this.envelopeId = envelopeId; }

    public String getSigningUrl() { return signingUrl; }
    public void setSigningUrl(String signingUrl) { this.signingUrl = signingUrl; }

    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public String getSignedDocumentUrl() { return signedDocumentUrl; }
    public void setSignedDocumentUrl(String signedDocumentUrl) { this.signedDocumentUrl = signedDocumentUrl; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public List<ContractMilestone> getMilestones() { return milestones; }
    public void setMilestones(List<ContractMilestone> milestones) { this.milestones = milestones; }

    public List<ContractClause> getClauses() { return clauses; }
    public void setClauses(List<ContractClause> clauses) {
        // FIX: ne jamais remplacer la référence — Hibernate suit l'instance originale
        this.clauses.clear();
        if (clauses != null) this.clauses.addAll(clauses);
    }

    public List<ContractHistory> getHistory() { return history; }
    public void setHistory(List<ContractHistory> history) { this.history = history; }

    public ContractDispute getDispute() { return dispute; }
    public void setDispute(ContractDispute dispute) { this.dispute = dispute; }

    public List<ContractDocument> getDocuments() { return documents; }
    public void setDocuments(List<ContractDocument> documents) { this.documents = documents; }

    public List<ContractPayment> getPayments() { return payments; }
    public void setPayments(List<ContractPayment> payments) { this.payments = payments; }
}