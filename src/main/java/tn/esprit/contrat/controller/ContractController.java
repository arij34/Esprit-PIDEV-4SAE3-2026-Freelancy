package tn.esprit.contrat.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.contrat.dto.ContractCreateRequest;
import tn.esprit.contrat.dto.ContractHistoryDTO;
import tn.esprit.contrat.dto.ContractResponseDTO;
import tn.esprit.contrat.dto.ContractUpdateRequest;
import tn.esprit.contrat.dto.MilestoneRequest;
import tn.esprit.contrat.entity.Contract;
import tn.esprit.contrat.entity.ContractMilestone;
import tn.esprit.contrat.entity.ContractStatus;
import tn.esprit.contrat.service.IContractService;
import tn.esprit.contrat.service.IDocuSignService;
import tn.esprit.contrat.service.ContractSigningScenarioService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final IContractService contractService;
    private final IDocuSignService docuSignService;
    private final ContractSigningScenarioService scenarioService;

    public ContractController(IContractService contractService,
                              IDocuSignService docuSignService,
                              ContractSigningScenarioService scenarioService) {
        this.contractService = contractService;
        this.docuSignService = docuSignService;
        this.scenarioService = scenarioService;
    }

    // =========================================================================
    // CRUD DE BASE
    // =========================================================================

    @GetMapping
    public ResponseEntity<List<ContractResponseDTO>> getAllContracts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(contractService.getAllContracts(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getContractById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return contractService.getAllContracts(authHeader).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ContractResponseDTO>> getByProject(
            @PathVariable Long projectId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(contractService.getContractsByProject(projectId, authHeader));
    }

    @PostMapping
    public ResponseEntity<?> createContract(
            @Valid @RequestBody ContractCreateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Contract created = contractService.createContract(request, authHeader);
        return contractService.getAllContracts(authHeader).stream()
                .filter(c -> c.getId().equals(created.getId()))
                .findFirst()
                .<ResponseEntity<?>>map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                .orElse(ResponseEntity.status(HttpStatus.CREATED).body(created));
    }

    @PostMapping("/internal")
    public ResponseEntity<Contract> createContractInternal(
            @RequestBody ContractCreateRequest request) {
        Contract created = contractService.createContractInternal(request);
        System.out.println("📋 Contrat créé #" + created.getId() +
                " statut : " + created.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contract> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Contract updated = contractService.updateContract(id, request, authHeader);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        contractService.deleteContract(id, authHeader);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // MILESTONES
    // =========================================================================

    @PostMapping("/{id}/milestones")
    public ResponseEntity<?> addMilestone(
            @PathVariable Long id,
            @RequestBody MilestoneRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            ContractMilestone milestone = contractService.addMilestone(id, request, authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(milestone);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage(), "type", e.getClass().getSimpleName()));
        }
    }

    @PutMapping("/{id}/milestones/{milestoneId}")
    public ResponseEntity<?> updateMilestone(
            @PathVariable Long id,
            @PathVariable Long milestoneId,
            @RequestBody MilestoneRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            ContractMilestone milestone = contractService.updateMilestone(id, milestoneId, request, authHeader);
            return ResponseEntity.ok(milestone);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/milestones/{milestoneId}")
    public ResponseEntity<?> deleteMilestone(
            @PathVariable Long id,
            @PathVariable Long milestoneId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            contractService.deleteMilestone(id, milestoneId, authHeader);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // CLAUSES
    // =========================================================================

    @PutMapping("/{id}/clauses/{clauseId}")
    public ResponseEntity<?> updateClause(
            @PathVariable Long id,
            @PathVariable Long clauseId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Object updatedClause = contractService.updateClause(id, clauseId, request, authHeader);
            return ResponseEntity.ok(updatedClause);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // RÉSUMÉ IA
    // =========================================================================

    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getContractSummary(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            List<String> summary = contractService.getContractSummary(id, authHeader);
            return ResponseEntity.ok(Map.of("contractId", id, "summary", summary));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // HISTORIQUE DES MODIFICATIONS
    // =========================================================================

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ContractHistoryDTO>> getContractHistory(@PathVariable Long id) {
        List<tn.esprit.contrat.entity.ContractHistory> history = contractService.getContractHistory(id);
        List<ContractHistoryDTO> dtoList = history.stream()
                .map(h -> new ContractHistoryDTO(
                        h.getId(),
                        h.getAction() != null ? h.getAction().name() : null,
                        h.getPerformedBy(),
                        h.getOldValue(),
                        h.getNewValue(),
                        h.getAiSummary(),
                        h.getPerformedAt()
                ))
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    // =========================================================================
    // WORKFLOW — CAS 1 : Freelancer accepte directement → PENDING_SIGNATURE
    // =========================================================================

    /**
     * PUT /api/contracts/{id}/accept
     * Le FREELANCER accepte la proposition du client telle quelle.
     * Transition : DRAFT → PENDING_SIGNATURE
     * → Email envoyé au freelancer pour qu'il signe en premier.
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptClientProposal(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Contract contract = contractService.getContractById(id);
            if (contract == null) return ResponseEntity.notFound().build();

            if (contract.getStatus() != ContractStatus.DRAFT) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Le contrat doit être en DRAFT pour être accepté. Statut : "
                                + contract.getStatus()
                ));
            }

            contract.setStatus(ContractStatus.PENDING_SIGNATURE);
            Contract saved = contractService.saveContract(contract);

            // ✅ Notifier le freelancer par email pour qu'il signe
            contractService.notifyFreelancerToSign(saved, authHeader);

            return ResponseEntity.ok(Map.of(
                    "message", "Proposition acceptée. Le contrat est en attente de signature.",
                    "contractId", saved.getId(),
                    "status", saved.getStatus().name(),
                    "title", saved.getTitle(),
                    "totalAmount", saved.getTotalAmount(),
                    "currency", saved.getCurrency() != null ? saved.getCurrency() : "TND",
                    "freelancerSignedAt", saved.getFreelancerSignedAt() != null
                            ? saved.getFreelancerSignedAt().toString() : "",
                    "clientSignedAt", saved.getClientSignedAt() != null
                            ? saved.getClientSignedAt().toString() : "",
                    "pdfUrl", saved.getPdfUrl() != null ? saved.getPdfUrl() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur interne : " + e.getMessage()
            ));
        }
    }

    // =========================================================================
    // WORKFLOW — CAS 2 : Freelancer demande des modifications
    // =========================================================================

    /**
     * PUT /api/contracts/{id}/send-modifications
     * Le FREELANCER envoie ses modifications au client.
     * Le contrat passe en DISPUTED. Email envoyé au client.
     */
    @PutMapping("/{id}/send-modifications")
    public ResponseEntity<?> sendModificationsToClient(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Contract contract = contractService.sendModificationsToClient(id, authHeader);

            return ResponseEntity.ok(Map.of(
                    "message", "Modifications envoyées au client par email.",
                    "contractId", contract.getId(),
                    "status", contract.getStatus().name()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur interne : " + e.getMessage()
            ));
        }
    }

    /**
     * PUT /api/contracts/{id}/accept-modifications
     * Le CLIENT accepte les modifications du freelancer.
     * Transition : DRAFT → PENDING_SIGNATURE
     * → Email envoyé au freelancer pour signer en premier.
     */
    @PutMapping("/{id}/accept-modifications")
    public ResponseEntity<?> clientAcceptModifications(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Déléguer toute la logique métier (vérifs, historique, emails) au service
            Contract saved = contractService.clientAcceptModifications(id, authHeader);

            return ResponseEntity.ok(Map.of(
                    "message", "Modifications acceptées. Le freelancer a été notifié pour signer.",
                    "contractId", saved.getId(),
                    "status", saved.getStatus().name(),
                    "freelancerSignedAt", saved.getFreelancerSignedAt() != null
                            ? saved.getFreelancerSignedAt().toString() : "",
                    "clientSignedAt", saved.getClientSignedAt() != null
                            ? saved.getClientSignedAt().toString() : "",
                    "pdfUrl", saved.getPdfUrl() != null ? saved.getPdfUrl() : ""
            ));
                    } catch (IllegalStateException e) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "error", e.getMessage()
                        ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur interne : " + e.getMessage()
            ));
        }
    }

    /**
     * PUT /api/contracts/{id}/reject-modifications
     * Le CLIENT refuse les modifications du freelancer.
     * Transition : DRAFT → DISPUTED
     * → Email de litige envoyé aux deux parties.
     */
    @PutMapping("/{id}/reject-modifications")
    public ResponseEntity<?> clientRejectModifications(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Utiliser le service qui gère la transition DISPUTED → CANCELLED + historique + emails
            Contract saved = contractService.clientRejectModifications(id, authHeader);

            return ResponseEntity.ok(Map.of(
                "message", "Modifications refusées. Le contrat est désormais fermé (CANCELLED).",
                "contractId", saved.getId(),
                "status", saved.getStatus().name()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur interne : " + e.getMessage()
            ));
        }
    }

    // =========================================================================
    // SIGNATURE
    // =========================================================================

    /**
     * PUT /api/contracts/{id}/sign?role=FREELANCER  (ou role=CLIENT)
     *
     * Règles :
     *  - Contrat doit être en PENDING_SIGNATURE
     *  - FREELANCER signe en premier
     *  - CLIENT signe après le freelancer
     *  - Quand les deux ont signé → ACTIVE + PDF généré + email aux deux parties avec PDF en PJ
     */
    @PutMapping("/{id}/sign")
        public ResponseEntity<?> signContract(
            @PathVariable Long id,
            @RequestParam String role,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, String> payload) {
        try {
            Contract contract = contractService.getContractById(id);
            if (contract == null) return ResponseEntity.notFound().build();

            if (contract.getStatus() != ContractStatus.PENDING_SIGNATURE) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Le contrat doit être en PENDING_SIGNATURE. Statut : "
                                + contract.getStatus()
                ));
            }

            if ("FREELANCER".equals(role)) {
                if (contract.getFreelancerSignedAt() != null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Le freelancer a déjà signé ce contrat."
                    ));
                }
            } else if ("CLIENT".equals(role)) {
                if (contract.getFreelancerSignedAt() == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Le freelancer doit signer en premier avant le client."
                    ));
                }
                if (contract.getClientSignedAt() != null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Le client a déjà signé ce contrat."
                    ));
                }
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Rôle invalide. Valeurs acceptées : FREELANCER, CLIENT."
                ));
            }

            String signatureImageData = payload != null ? payload.get("signatureImageData") : null;

            // ✅ Déléguer au service (emails + PDF)
            Contract saved = contractService.signContract(id, role, authHeader, signatureImageData);

            String message;
            if (saved.getStatus() == ContractStatus.ACTIVE) {
                message = "Les deux parties ont signé. Le contrat est ACTIF. PDF généré et envoyé par email.";
            } else if ("FREELANCER".equals(role)) {
                message = "Signature enregistrée. Le client a été notifié par email pour signer.";
            } else {
                message = "Signature enregistrée.";
            }

            return ResponseEntity.ok(Map.of(
                    "message", message,
                    "contractId", saved.getId(),
                    "status", saved.getStatus().name(),
                    "freelancerSignedAt", saved.getFreelancerSignedAt() != null
                            ? saved.getFreelancerSignedAt().toString() : "",
                    "clientSignedAt", saved.getClientSignedAt() != null
                            ? saved.getClientSignedAt().toString() : "",
                    "pdfUrl", saved.getPdfUrl() != null ? saved.getPdfUrl() : ""
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 affiche toute l'erreur dans la console

            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erreur interne : " + e.getMessage(),
                    "cause", e.getCause() != null ? e.getCause().getMessage() : "unknown"
            ));
        }
    }

    /**
     * PUT /api/contracts/{id}/submit
     * Transition : DRAFT → PENDING_SIGNATURE (endpoint de compatibilité)
     */
    @PutMapping("/{id}/submit")
    public ResponseEntity<?> submitForSignature(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Contract contract = contractService.submitForSignature(id, authHeader);
            return ResponseEntity.ok(Map.of(
                    "message", "Contrat soumis pour signature.",
                    "contractId", contract.getId(),
                    "status", contract.getStatus().name()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // DOCUSIGN
    // =========================================================================

    @PostMapping("/{id}/send-for-signature")
    public ResponseEntity<?> sendForDocuSignature(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String signerEmail = request.get("signerEmail");
            String signerName  = request.get("signerName");
            if (signerEmail == null || signerName == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Champs requis : signerEmail, signerName"
                ));
            }
            Contract contract = contractService.getContractById(id);
            if (contract == null) return ResponseEntity.notFound().build();

            String signingUrl = docuSignService.sendForSignature(contract, signerEmail, signerName);
            return ResponseEntity.ok(Map.of(
                    "message", "Contrat envoyé pour signature via DocuSign.",
                    "contractId", id,
                    "envelopeId", contract.getEnvelopeId(),
                    "signingUrl", signingUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/signature-status")
    public ResponseEntity<?> getSignatureStatus(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Contract contract = contractService.getContractById(id);
            if (contract == null) return ResponseEntity.notFound().build();
            var statusMap = docuSignService.getSignatureStatus(contract);
            return ResponseEntity.ok(Map.of(
                    "contractId", id,
                    "envelopeId", contract.getEnvelopeId(),
                    "signatureStatus", contract.getSignatureStatus(),
                    "docuSignStatus", statusMap.get("status"),
                    "signedAt", contract.getSignedAt() != null ? contract.getSignedAt().toString() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/signature-webhook")
    public ResponseEntity<?> handleSignatureWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String envelopeId = (String) payload.get("envelopeId");
            if (envelopeId == null)
                return ResponseEntity.badRequest().body(Map.of("error", "envelopeId manquant"));
            docuSignService.handleSignatureWebhook(envelopeId);
            return ResponseEntity.ok(Map.of("message", "Webhook traité."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/download-signed")
    public ResponseEntity<?> downloadSignedDocument(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Contract contract = contractService.getContractById(id);
            if (contract == null) return ResponseEntity.notFound().build();
            byte[] documentBytes = docuSignService.downloadSignedDocument(contract);
            return ResponseEntity.ok()
                    .header("Content-Disposition",
                            "attachment; filename=\"" + contract.getTitle() + "_signed.pdf\"")
                    .header("Content-Type", "application/pdf")
                    .body(documentBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // TEST (à désactiver en production)
    // =========================================================================

    @GetMapping("/{id}/test-complete-scenario")
    public ResponseEntity<?> testCompleteSigningScenario(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Map<String, Object> result = scenarioService.executeCompleteSigningScenario(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(), "type", e.getClass().getSimpleName()
            ));
        }
    }
}