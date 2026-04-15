package tn.esprit.contrat.entity;

public enum ContractAction {
    CREATED,
    UPDATED,
    STATUS_CHANGED,
    CLIENT_SIGNED,
    FREELANCER_SIGNED,
    MILESTONE_ADDED,
    MILESTONE_COMPLETED,
    MILESTONE_APPROVED,
    PAYMENT_RELEASED,
    DISPUTE_OPENED,
    DISPUTE_RESOLVED,
    PDF_GENERATED,
    CANCELLED,
    SUBMITTED,              // Client soumet pour signature → PENDING_SIGNATURE
    SIGNED_BY_FREELANCER,   // Freelancer signe
    SIGNED_BY_CLIENT,       // Client signe
    ACTIVATED,              // Les deux ont signé → ACTIVE
    MODIFICATIONS_SENT,     // Freelancer envoie ses modifications au client
    MODIFICATIONS_ACCEPTED, // Client accepte les modifications → PENDING_SIGNATURE
    MODIFICATIONS_REJECTED , // Client refuse les modifications → DISPUTED
    MODIFICATIONS_PROPOSED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED
}