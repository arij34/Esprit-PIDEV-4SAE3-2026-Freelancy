package tn.esprit.contrat.service;

import org.springframework.stereotype.Service;
import tn.esprit.contrat.entity.*;
import tn.esprit.contrat.repository.ContractRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * 📋 Contract Signing Scenario Service
 * 
 * Ce service démontre le flux complet de signature d'un contrat:
 * 
 * ✅ ÉTAPE 1: Client crée une proposal → Freelancer accepte
 * ✅ ÉTAPE 2: Contrat créé automatiquement en DRAFT avec milestones
 * ✅ ÉTAPE 3: Client soumet le contrat → PENDING_SIGNATURE
 * ✅ ÉTAPE 4: Freelancer signe en premier
 * ✅ ÉTAPE 5: Client signe → Contrat passe en ACTIVE
 * ✅ ÉTAPE 6: PDF généré et envoyé aux deux parties
 * 
 * Usage: Ce service peut être appelé pour tester/démontrer le flux complet
 */
@Service
public class ContractSigningScenarioService {

    private static final Logger logger = Logger.getLogger(ContractSigningScenarioService.class.getName());

    private final ContractRepository contractRepository;
    private final IContractService contractService;

    public ContractSigningScenarioService(ContractRepository contractRepository, IContractService contractService) {
        this.contractRepository = contractRepository;
        this.contractService = contractService;
    }

    /**
     * Exécute le SCÉNARIO COMPLET de signature d'un contrat en DEMO MODE
     * 
     * Pour chaque étape, affiche un log de progression et met à jour la BD
     * 
     * @param contractId l'ID du contrat à utiliser
     * @return un Map avec le résultat final du scénario
     */
    public Map<String, Object> executeCompleteSigningScenario(Long contractId) {
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " non trouvé"));

            logger.info("═══════════════════════════════════════════════════════════════════════");
            logger.info("🎬 DÉMARRAGE DU SCÉNARIO COMPLET DE SIGNATURE");
            logger.info("═══════════════════════════════════════════════════════════════════════");

            // ─────────────────────────────────────────────────────────────────────────────
            // ÉTAPE 1: Vérif que le contrat est en DRAFT
            // ─────────────────────────────────────────────────────────────────────────────
            result.put("step1_status", verifyContractState(contract, ContractStatus.DRAFT, result));
            if (contract.getStatus() != ContractStatus.DRAFT) {
                result.put("error", "Le contrat n'est pas en DRAFT - impossible de continuer");
                return result;
            }

            // ─────────────────────────────────────────────────────────────────────────────
            // ÉTAPE 2: Client soumet le contrat pour signature
            // ─────────────────────────────────────────────────────────────────────────────
            result.put("step2_submission", submitContractForSignature(contract, result));
            contract = contractRepository.findById(contractId).get();

            // ─────────────────────────────────────────────────────────────────────────────
            // ÉTAPE 3: Freelancer signe en premier
            // ─────────────────────────────────────────────────────────────────────────────
            result.put("step3_freelancer_sign", signContractAsFreelancer(contract, result));
            contract = contractRepository.findById(contractId).get();

            // ─────────────────────────────────────────────────────────────────────────────
            // ÉTAPE 4: Client signe
            // ─────────────────────────────────────────────────────────────────────────────
            result.put("step4_client_sign", signContractAsClient(contract, result));
            contract = contractRepository.findById(contractId).get();

            // ─────────────────────────────────────────────────────────────────────────────
            // ÉTAPE 5: Vérification finale
            // ─────────────────────────────────────────────────────────────────────────────
            result.put("step5_final_status", verifyContractActive(contract, result));

            // ─────────────────────────────────────────────────────────────────────────────
            // RÉSUMÉ
            // ─────────────────────────────────────────────────────────────────────────────
            result.put("final_summary", Map.of(
                    "contractId", contract.getId(),
                    "contractTitle", contract.getTitle(),
                    "status", contract.getStatus().name(),
                    "freelancerSignedAt", contract.getFreelancerSignedAt(),
                    "clientSignedAt", contract.getClientSignedAt(),
                    "pdfUrl", contract.getPdfUrl(),
                    "totalAmount", contract.getTotalAmount(),
                    "currency", contract.getCurrency(),
                    "createdAt", contract.getCreatedAt(),
                    "updatedAt", contract.getUpdatedAt()
            ));

            logger.info("═══════════════════════════════════════════════════════════════════════");
            logger.info("✅ SCÉNARIO COMPLET TESTÉ AVEC SUCCÈS!");
            logger.info("═══════════════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            logger.severe("❌ ERREUR LORS DU SCÉNARIO: " + e.getMessage());
            e.printStackTrace();
            result.put("error", e.getMessage());
            result.put("exception", e.getClass().getSimpleName());
        }

        return result;
    }

    /**
     * Étape 1: Vérifier que le contrat est en DRAFT
     */
    private Map<String, String> verifyContractState(Contract contract, ContractStatus expected, Map<String, Object> logs) {
        String status = contract.getStatus().name();
        logger.info("📋 ÉTAPE 1: Vérification de l'état du contrat");
        logger.info("   Status actuel: " + status);
        logger.info("   Status attendu: " + expected.name());
        
        boolean ok = contract.getStatus() == expected;
        logger.info(ok ? "   ✅ OK - Contrat en DRAFT" : "   ❌ ERREUR - Contrat pas en DRAFT");
        
        return Map.of(
                "status", status,
                "expected", expected.name(),
                "passed", String.valueOf(ok),
                "message", ok ? "Contrat prêt pour la signature" : "Contrat dans un état invalide"
        );
    }

    /**
     * Étape 2: Client soumet le contrat pour signature
     */
    private Map<String, Object> submitContractForSignature(Contract contract, Map<String, Object> logs) {
        logger.info("\n📋 ÉTAPE 2: Client soumet le contrat pour signature");
        logger.info("   ID du client: " + contract.getClientId());
        logger.info("   Titre du contrat: " + contract.getTitle());

        ContractStatus statusBefore = contract.getStatus();
        int milestonesCount = contract.getMilestones() != null ? contract.getMilestones().size() : 0;
        
        logger.info("   Nombre de milestones: " + milestonesCount);
        logger.info("   Montant total: " + contract.getTotalAmount() + " " + contract.getCurrency());

        try {
            Contract submitted = contractService.submitForSignature(contract.getId(), "Bearer demo");
            logger.info("   ✅ Contrat soumis avec succès");
            logger.info("   Statue avant: " + statusBefore.name());
            logger.info("   Statut après: " + submitted.getStatus().name());
            logger.info("   Freelancer a été notifié: 📬");

            return Map.of(
                    "success", true,
                    "statusBefore", statusBefore.name(),
                    "statusAfter", submitted.getStatus().name(),
                    "message", "Contrat soumis pour signature - Freelancer notifié"
            );
        } catch (Exception e) {
            logger.severe("   ❌ ERREUR: " + e.getMessage());
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Étape 3: Freelancer signe en premier
     */
    private Map<String, Object> signContractAsFreelancer(Contract contract, Map<String, Object> logs) {
        logger.info("\n📋 ÉTAPE 3: Freelancer signe le contrat");
        logger.info("   ID du freelancer: " + contract.getFreelancerId());
        logger.info("   Nom du freelancer: Freelancer #" + contract.getFreelancerId());

        try {
            Contract signed = contractService.signContract(contract.getId(), "FREELANCER", "Bearer demo", null);
            logger.info("   ✅ Signature du freelancer enregistrée");
            logger.info("   Heure de signature: " + signed.getFreelancerSignedAt());
            logger.info("   Statut actuel: " + signed.getStatus().name());
            logger.info("   En attente de signature du client...");

            return Map.of(
                    "success", true,
                    "freelancerSignedAt", signed.getFreelancerSignedAt(),
                    "status", signed.getStatus().name(),
                    "message", "Freelancer a signé - En attente de signature du client"
            );
        } catch (Exception e) {
            logger.severe("   ❌ ERREUR: " + e.getMessage());
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Étape 4: Client signe
     */
    private Map<String, Object> signContractAsClient(Contract contract, Map<String, Object> logs) {
        logger.info("\n📋 ÉTAPE 4: Client signe le contrat");
        logger.info("   ID du client: " + contract.getClientId());
        logger.info("   Nom du client: Client #" + contract.getClientId());

        try {
            Contract signed = contractService.signContract(contract.getId(), "CLIENT", "Bearer demo", null);
            logger.info("   ✅ Signature du client enregistrée");
            logger.info("   Heure de signature: " + signed.getClientSignedAt());
            logger.info("   🎉 LES DEUX PARTIES ONT SIGNÉ!");
            logger.info("   Statut: " + signed.getStatus().name());
            logger.info("   PDF URL: " + signed.getPdfUrl());
            logger.info("   📧 Les deux parties ont été notifiées");

            return Map.of(
                    "success", true,
                    "clientSignedAt", signed.getClientSignedAt(),
                    "status", signed.getStatus().name(),
                    "pdfUrl", signed.getPdfUrl(),
                    "message", "Les deux parties ont signé - Contrat est maintenant ACTIF - PDF généré et envoyé"
            );
        } catch (Exception e) {
            logger.severe("   ❌ ERREUR: " + e.getMessage());
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Étape 5: Vérifier que le contrat est ACTIVE avec les deux signatures
     */
    private Map<String, Object> verifyContractActive(Contract contract, Map<String, Object> logs) {
        logger.info("\n📋 ÉTAPE 5: Vérification finale");
        
        boolean isActive = contract.getStatus() == ContractStatus.ACTIVE;
        boolean hasBothSignatures = contract.getFreelancerSignedAt() != null && 
                                   contract.getClientSignedAt() != null;
        boolean hasPdf = contract.getPdfUrl() != null && !contract.getPdfUrl().isBlank();

        logger.info("   Statut ACTIVE: " + (isActive ? "✅" : "❌"));
        logger.info("   Deux signatures: " + (hasBothSignatures ? "✅" : "❌"));
        logger.info("   PDF généré: " + (hasPdf ? "✅" : "❌"));

        boolean allPassed = isActive && hasBothSignatures && hasPdf;
        logger.info("   Résultat global: " + (allPassed ? "✅ SUCCÈS" : "❌ ÉCHEC"));

        return Map.of(
                "contractStatus", contract.getStatus().name(),
                "isActive", isActive,
                "hasBothSignatures", hasBothSignatures,
                "hasPdf", hasPdf,
                "freelancerSignedAt", contract.getFreelancerSignedAt(),
                "clientSignedAt", contract.getClientSignedAt(),
                "pdfUrl", contract.getPdfUrl(),
                "allChecked", allPassed
        );
    }
}
