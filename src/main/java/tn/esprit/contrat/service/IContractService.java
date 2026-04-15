package tn.esprit.contrat.service;

import tn.esprit.contrat.dto.ContractCreateRequest;
import tn.esprit.contrat.dto.ContractResponseDTO;
import tn.esprit.contrat.dto.ContractUpdateRequest;
import tn.esprit.contrat.dto.MilestoneRequest;
import tn.esprit.contrat.entity.Contract;
import tn.esprit.contrat.entity.ContractMilestone;

import java.util.List;

public interface IContractService {

    /** Retourne tous les contrats */
    List<ContractResponseDTO> getAllContracts(String authHeader);

    /** Retourne les contrats d'un projet */
    List<ContractResponseDTO> getContractsByProject(Long projectId, String authHeader);

    /** Retourne un contrat par son ID */
    Contract getContractById(Long contractId);

    /** Crée un contrat depuis le frontend (avec vérification rôle) */
    Contract createContract(ContractCreateRequest request, String authHeader);

    /**
     * Crée un contrat automatiquement depuis ProposalController
     * Sans vérification de rôle — appel interne inter-microservices
     */
    Contract createContractInternal(ContractCreateRequest request);

    /** Modifie un contrat (DRAFT uniquement) */
    Contract updateContract(Long contractId, ContractUpdateRequest request, String authHeader);

    /** Supprime un contrat (DRAFT uniquement) */
    void deleteContract(Long contractId, String authHeader);

    // =========================================================================
    // ── Milestones Management ───────────────────────────────────────────────
    // =========================================================================

    /** Ajoute un milestone à un contrat */
    ContractMilestone addMilestone(Long contractId, MilestoneRequest request, String authHeader);

    /** Modifie un milestone existant */
    ContractMilestone updateMilestone(Long contractId, Long milestoneId, MilestoneRequest request, String authHeader);

    /** Supprime un milestone */
    void deleteMilestone(Long contractId, Long milestoneId, String authHeader);

    // =========================================================================
    // ── Clauses du contrat ───────────────────────────────────────────────────
    // =========================================================================

    /** Met à jour le texte d'une clause */
    Object updateClause(Long contractId, Long clauseId, Object clauseUpdateRequest, String authHeader);

    // =========================================================================
    // ── Étape 2 — Configuration et signature ─────────────────────────────────
    // =========================================================================

    /**
     * Le client soumet le contrat pour signature.
     * Vérifie que la somme des milestones == totalAmount,
     * passe le statut DRAFT → PENDING_SIGNATURE et notifie le freelancer.
     */
    Contract submitForSignature(Long contractId, String authHeader);

    /**
     * Retourne le résumé AI du contrat en 5 points (pour le freelancer).
     * Appelle Claude pour générer un résumé lisible du contrat.
     */
    List<String> getContractSummary(Long contractId, String authHeader);

    /**
     * Signature du contrat.
     * @param signerRole "FREELANCER" ou "CLIENT"
     * Lorsque les deux parties ont signé → statut ACTIVE + génération PDF.
     */
    Contract signContract(Long contractId, String signerRole, String authHeader, String signatureImageData);

    /**
     * Cas 1 — Le client accepte la proposition initiale du contrat (DRAFT)
     * et le soumet directement pour signature → PENDING_SIGNATURE.
     * Équivalent de submitForSignature() mais déclenché côté client via "accepter".
     */
    Contract clientAcceptContract(Long contractId, String authHeader);

    /**
     * Le freelancer envoie ses modifications au client.
     * Statut reste DRAFT mais une notification est envoyée au client.
     */
    Contract sendModificationsToClient(Long contractId, String authHeader);

    /**
     * Le client accepte les modifications du freelancer → PENDING_SIGNATURE.
     * Envoie une notification au freelancer pour qu'il signe.
     */
    Contract clientAcceptModifications(Long contractId, String authHeader);

    /**
     * Le client refuse les modifications → statut DISPUTED.
     */
    Contract clientRejectModifications(Long contractId, String authHeader);

    Contract saveContract(Contract contract);

    void notifyClientOfModifications(Contract contract, String authHeader);

    void notifyFreelancerToSign(Contract contract, String authHeader);

    void generatePdfAndNotify(Contract contract, String authHeader);

    /** Retourne l'historique complet du contrat (dernier évènement en premier). */
    java.util.List<tn.esprit.contrat.entity.ContractHistory> getContractHistory(Long contractId);
}