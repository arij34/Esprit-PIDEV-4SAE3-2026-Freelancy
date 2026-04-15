package tn.esprit.contrat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract_disputes")
public class ContractDispute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status = DisputeStatus.OPEN;

    // ID de l'utilisateur qui a ouvert le litige
    @Column(nullable = false)
    private Long openedBy;

    // ID de l'admin qui a résolu le litige
    private Long resolvedBy;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(nullable = false, updatable = false)
    private LocalDateTime openedAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        openedAt = LocalDateTime.now();
    }

    // ─── Getters / Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DisputeStatus getStatus() { return status; }
    public void setStatus(DisputeStatus status) { this.status = status; }

    public Long getOpenedBy() { return openedBy; }
    public void setOpenedBy(Long openedBy) { this.openedBy = openedBy; }

    public Long getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(Long resolvedBy) { this.resolvedBy = resolvedBy; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public LocalDateTime getOpenedAt() { return openedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}