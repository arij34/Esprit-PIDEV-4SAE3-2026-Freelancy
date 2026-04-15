package tn.esprit.contrat.service;

import jakarta.mail.MessagingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.contrat.clients.UserServiceClient;
import tn.esprit.contrat.dto.*;
import tn.esprit.contrat.entity.*;
import tn.esprit.contrat.repository.ContractHistoryRepository;
import tn.esprit.contrat.repository.ContractRepository;
import tn.esprit.contrat.repository.ContractMilestoneRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContractServiceImpl implements IContractService {

    private final ContractRepository        contractRepo;
    private final ContractMilestoneRepository milestoneRepo;
    private final ContractHistoryRepository historyRepo;
    private final ClaudeAiService           claudeAiService;
    private final PdfGeneratorService       pdfGeneratorService;
    private final UserServiceClient         userServiceClient;
    private final EmailService              emailService;

    private static final Long SYSTEM_USER_ID = 0L;

    public ContractServiceImpl(ContractRepository contractRepo,
                               ContractMilestoneRepository milestoneRepo,
                               ContractHistoryRepository historyRepo,
                               ClaudeAiService claudeAiService,
                               PdfGeneratorService pdfGeneratorService,
                               UserServiceClient userServiceClient,
                               EmailService emailService) {
        this.contractRepo        = contractRepo;
        this.milestoneRepo       = milestoneRepo;
        this.historyRepo         = historyRepo;
        this.claudeAiService     = claudeAiService;
        this.pdfGeneratorService = pdfGeneratorService;
        this.userServiceClient   = userServiceClient;
        this.emailService        = emailService;
    }

    // =========================================================================
    // GET ALL
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getAllContracts(String authHeader) {
        Map<Long, String> userNames = loadUserNames(authHeader);
        return contractRepo.findAllWithClauses().stream()
                .map(c -> toDTO(c, userNames))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // GET BY PROJECT
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getContractsByProject(Long projectId, String authHeader) {
        Map<Long, String> userNames = loadUserNames(authHeader);
        List<Contract> projectContracts = contractRepo.findAllWithClauses().stream()
                .filter(c -> c.getProjectId() != null && c.getProjectId().equals(projectId))
                .collect(Collectors.toList());
        return projectContracts.stream()
                .map(c -> toDTO(c, userNames))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // GET CONTRACT BY ID
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public Contract getContractById(Long contractId) {
        return contractRepo.findById(contractId).orElse(null);
    }

    // =========================================================================
    // CREATE (frontend)
    // =========================================================================
    @Override
    public Contract createContract(ContractCreateRequest request, String authHeader) {
        return createContractInternal(request);
    }

    // =========================================================================
    // CREATE INTERNAL
    // =========================================================================
    @Override
    public Contract createContractInternal(ContractCreateRequest request) {

        if (request.getProposalId() != null
                && contractRepo.existsByProposalId(request.getProposalId())) {
            throw new IllegalStateException(
                    "Un contrat existe déjà pour la proposition #" + request.getProposalId());
        }

        LocalDate effectiveStart = request.getStartDate() != null
                ? request.getStartDate() : LocalDate.now();
        LocalDate effectiveEnd = request.getEndDate() != null
                ? request.getEndDate() : effectiveStart.plusMonths(3);

        AiContractResult aiResult;
        try {
            aiResult = claudeAiService.generateContractContent(
                    request.getTitle(),
                    request.getDescription(),
                    request.getTotalAmount() != null ? request.getTotalAmount() : BigDecimal.ZERO,
                    request.getCurrency() != null ? request.getCurrency() : "TND",
                    effectiveStart,
                    effectiveEnd
            );
            System.out.println("✅ [CLAUDE AI] Contenu généré avec succès");
        } catch (Exception aiEx) {
            System.err.println("⚠️ [CLAUDE AI] Échec génération — fallback: " + aiEx.getMessage());
            aiResult = new AiContractResult(null, null, null);
        }

        Contract contract = new Contract();
        contract.setTitle(request.getTitle());
        contract.setDescription(buildFinalDescription(
                request.getDescription(),
                aiResult.getGeneratedDescription(),
                aiResult.getClauses()
        ));
        contract.setProjectId(request.getProjectId());
        contract.setProposalId(request.getProposalId());
        contract.setFreelancerId(request.getFreelancerId());
        contract.setFreelancerKeycloakId("system");
        contract.setClientId(request.getClientId() != null ? request.getClientId() : SYSTEM_USER_ID);
        contract.setTotalAmount(request.getTotalAmount());
        contract.setCurrency(request.getCurrency() != null ? request.getCurrency() : "TND");
        contract.setStartDate(effectiveStart);
        contract.setEndDate(effectiveEnd);
        contract.setDeadline(request.getDeadline());
        contract.setStatus(ContractStatus.DRAFT);

        Contract saved = contractRepo.save(contract);

        List<MilestoneRequest> aiMilestones = aiResult.getSuggestedMilestones();
        if (aiMilestones == null || aiMilestones.isEmpty()) {
            System.out.println("⚠️ [MILESTONE] Fallback 5 phases");
            aiMilestones = claudeAiService.buildFallbackMilestones(
                    request.getTotalAmount() != null ? request.getTotalAmount() : BigDecimal.ZERO,
                    effectiveStart,
                    effectiveEnd
            );
        }

        System.out.println("📋 [MILESTONE] " + aiMilestones.size() + " milestones à persister");
        for (int i = 0; i < aiMilestones.size(); i++) {
            MilestoneRequest mr = aiMilestones.get(i);
            ContractMilestone milestone = new ContractMilestone();
            milestone.setContract(saved);
            milestone.setTitle(mr.getTitle());
            milestone.setDescription(mr.getDescription());
            milestone.setAmount(mr.getAmount());
            milestone.setOrderIndex(i + 1);
            milestone.setStatus(MilestoneStatus.PENDING);
            if (mr.getDeadline() != null && !mr.getDeadline().isBlank())
                milestone.setDeadline(LocalDate.parse(mr.getDeadline()));
            saved.getMilestones().add(milestone);
        }

        ContractHistory history = new ContractHistory();
        history.setContract(saved);
        history.setAction(ContractAction.CREATED);
        history.setPerformedBy(SYSTEM_USER_ID);
        history.setNewValue("Contrat créé avec " + aiMilestones.size() + " milestones");
        historyRepo.save(history);

        persistDefaultClausesIfNeeded(saved);

        // 🔒 Force DRAFT quoi qu'il arrive (même si un statut a été modifié plus haut)
        saved.setStatus(ContractStatus.DRAFT);
        return contractRepo.save(saved);
    }

    // =========================================================================
    // UPDATE (DRAFT uniquement)
    // =========================================================================
    @Override
    public Contract updateContract(Long id, ContractUpdateRequest request, String authHeader) {

        Contract contract = contractRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat #" + id + " introuvable"));

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new IllegalStateException("Seuls les contrats en DRAFT peuvent être modifiés.");
        }

        if (request.getTitle()       != null) contract.setTitle(request.getTitle());
        if (request.getDescription() != null) contract.setDescription(request.getDescription());
        if (request.getTotalAmount() != null) contract.setTotalAmount(request.getTotalAmount());
        if (request.getCurrency()    != null) contract.setCurrency(request.getCurrency());

        if (isValidDate(request.getStartDate())) contract.setStartDate(LocalDate.parse(request.getStartDate()));
        if (isValidDate(request.getEndDate()))   contract.setEndDate(LocalDate.parse(request.getEndDate()));
        if (isValidDate(request.getDeadline()))  contract.setDeadline(LocalDate.parse(request.getDeadline()));
        else                                      contract.setDeadline(null);

        if (request.getMilestones() != null) {
            contract.getMilestones().clear();
            contractRepo.saveAndFlush(contract);

            for (int i = 0; i < request.getMilestones().size(); i++) {
                MilestoneRequest mr = request.getMilestones().get(i);
                if (mr.getTitle() == null || mr.getTitle().isBlank()) continue;

                ContractMilestone milestone = new ContractMilestone();
                milestone.setContract(contract);
                milestone.setTitle(mr.getTitle().trim());
                milestone.setDescription(mr.getDescription());
                milestone.setAmount(mr.getAmount());
                milestone.setOrderIndex(i + 1);
                milestone.setStatus(MilestoneStatus.PENDING);
                if (isValidDate(mr.getDeadline()))
                    milestone.setDeadline(LocalDate.parse(mr.getDeadline()));
                contract.getMilestones().add(milestone);
            }
        }

        ContractHistory history = new ContractHistory();
        history.setContract(contract);
        history.setAction(ContractAction.UPDATED);
        history.setPerformedBy(SYSTEM_USER_ID);
        history.setNewValue("Contrat modifié — " + contract.getMilestones().size() + " milestones");
        historyRepo.save(history);

        return contractRepo.save(contract);
    }

    // =========================================================================
    // DELETE (DRAFT uniquement)
    // =========================================================================
    @Override
    public void deleteContract(Long id, String authHeader) {
        Contract contract = contractRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat #" + id + " introuvable"));
        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new IllegalStateException("Seuls les contrats en DRAFT peuvent être supprimés.");
        }
        contractRepo.delete(contract);
    }

    // =========================================================================
    // SOUMETTRE POUR SIGNATURE
    // =========================================================================
    @Override
    public Contract submitForSignature(Long contractId, String authHeader) {

        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new IllegalStateException(
                    "Seuls les contrats en DRAFT peuvent être soumis pour signature. Statut : "
                            + contract.getStatus());
        }

        if (contract.getMilestones() == null || contract.getMilestones().isEmpty()) {
            throw new IllegalStateException(
                    "Le contrat doit avoir au moins un milestone avant d'être soumis.");
        }

        BigDecimal milestonesTotal = contract.getMilestones().stream()
                .map(ContractMilestone::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (milestonesTotal.compareTo(contract.getTotalAmount()) != 0) {
            throw new IllegalStateException(
                    "La somme des milestones (" + milestonesTotal + ") ≠ montant total ("
                            + contract.getTotalAmount() + ").");
        }

        contract.setStatus(ContractStatus.PENDING_SIGNATURE);

        ContractHistory history = new ContractHistory();
        history.setContract(contract);
        history.setAction(ContractAction.SUBMITTED);
        history.setPerformedBy(contract.getClientId() != null ? contract.getClientId() : SYSTEM_USER_ID);
        history.setOldValue(ContractStatus.DRAFT.name());
        history.setNewValue(ContractStatus.PENDING_SIGNATURE.name());
        historyRepo.save(history);

        Contract saved = contractRepo.save(contract);

        // Notifier le freelancer par email
        try {
            emailService.sendSignatureRequestToFreelancer(saved, authHeader);
        } catch (Exception e) {
            System.err.println("⚠️ Erreur envoi email freelancer : " + e.getMessage());
        }

        return saved;
    }

    // =========================================================================
    // RÉSUMÉ AI
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public List<String> getContractSummary(Long contractId, String authHeader) {

        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        if (contract.getStatus() == ContractStatus.DRAFT) {
            throw new IllegalStateException(
                    "Le résumé AI est disponible seulement après soumission pour signature.");
        }

        return claudeAiService.summarizeContract(
                contract.getTitle(),
                contract.getDescription(),
                contract.getMilestones(),
                contract.getTotalAmount(),
                contract.getCurrency()
        );
    }

    // =========================================================================
    // SIGNATURE
    // =========================================================================
    @Override
    public Contract signContract(Long contractId, String signerRole, String authHeader, String signatureImageData) {

        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        if (contract.getStatus() != ContractStatus.PENDING_SIGNATURE) {
            throw new IllegalStateException(
                    "Le contrat doit être en PENDING_SIGNATURE. Statut actuel : "
                            + contract.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();

        switch (signerRole.toUpperCase()) {
            case "FREELANCER" -> {
                if (contract.getFreelancerSignedAt() != null)
                    throw new IllegalStateException("Le freelancer a déjà signé ce contrat.");
                contract.setFreelancerSignedAt(now);

                // Sauvegarder l'image de signature si fournie
                if (signatureImageData != null && !signatureImageData.isBlank()) {
                    try {
                        String path = saveSignatureImage(contractId, "freelancer", signatureImageData);
                        contract.setFreelancerSignatureImagePath(path);
                    } catch (Exception e) {
                        System.err.println("⚠️ [SIGNATURE IMG] Impossible d'enregistrer la signature freelancer : " + e.getMessage());
                    }
                }

                ContractHistory h = new ContractHistory();
                h.setContract(contract);
                h.setAction(ContractAction.SIGNED_BY_FREELANCER);
                h.setPerformedBy(contract.getFreelancerId() != null ? contract.getFreelancerId() : SYSTEM_USER_ID);
                h.setNewValue("Freelancer a signé le " + now);
                historyRepo.save(h);

                // Notifier le client par email pour qu'il signe
                try {
                    emailService.sendSignatureRequestToClient(contract, authHeader);
                    System.out.println("✅ [EMAIL] Notification envoyée au client pour signer");
                } catch (Exception e) {
                    System.err.println("⚠️ [EMAIL] Erreur notification client : " + e.getMessage());
                }
            }
            case "CLIENT" -> {
                if (contract.getFreelancerSignedAt() == null)
                    throw new IllegalStateException("Le freelancer doit signer en premier.");
                if (contract.getClientSignedAt() != null)
                    throw new IllegalStateException("Le client a déjà signé ce contrat.");
                contract.setClientSignedAt(now);

                // Sauvegarder l'image de signature si fournie
                if (signatureImageData != null && !signatureImageData.isBlank()) {
                    try {
                        String path = saveSignatureImage(contractId, "client", signatureImageData);
                        contract.setClientSignatureImagePath(path);
                    } catch (Exception e) {
                        System.err.println("⚠️ [SIGNATURE IMG] Impossible d'enregistrer la signature client : " + e.getMessage());
                    }
                }

                ContractHistory h = new ContractHistory();
                h.setContract(contract);
                h.setAction(ContractAction.SIGNED_BY_CLIENT);
                h.setPerformedBy(contract.getClientId() != null ? contract.getClientId() : SYSTEM_USER_ID);
                h.setNewValue("Client a signé le " + now);
                historyRepo.save(h);
            }
            default -> throw new IllegalArgumentException(
                    "Rôle invalide : '" + signerRole + "'. Valeurs : FREELANCER, CLIENT");
        }

        // Si les deux ont signé → ACTIVE + PDF + email
        if (contract.getFreelancerSignedAt() != null && contract.getClientSignedAt() != null) {
            contract.setStatus(ContractStatus.ACTIVE);

            // Save FIRST before PDF generation
            Contract saved = contractRepo.save(contract);

            // PDF generation - fully isolated
            try {
                // Charger les noms lisibles pour le client et le freelancer
                Map<Long, String> userNames = loadUserNames(authHeader);
                String clientName = resolveUserDisplayName(
                        saved.getClientId(), userNames, authHeader, "Client");
                String freelancerName = resolveUserDisplayName(
                        saved.getFreelancerId(), userNames, authHeader, "Freelancer");

                String pdfPath = pdfGeneratorService.generateContractPdf(
                        saved,
                        clientName,
                        freelancerName,
                        saved.getTitle()
                );
                saved.setPdfUrl(pdfPath);
                saved = contractRepo.save(saved);
                System.out.println("✅ [PDF] Généré : " + pdfPath);
            } catch (Exception e) {
                System.err.println("⚠️ [PDF] Erreur génération : " + e.getMessage());
                // Don't rethrow - contract is already ACTIVE
            }

            // History
            ContractHistory activation = new ContractHistory();
            activation.setContract(saved);
            activation.setAction(ContractAction.ACTIVATED);
            activation.setPerformedBy(SYSTEM_USER_ID);
            activation.setOldValue(ContractStatus.PENDING_SIGNATURE.name());
            activation.setNewValue(ContractStatus.ACTIVE.name());
            historyRepo.save(activation);

            // Email - fully isolated
            try {
                emailService.sendSignedContractToParties(saved, authHeader);
            } catch (Exception e) {
                System.err.println("⚠️ [EMAIL] Erreur : " + e.getMessage());
            }

            return saved;
        }

        return contractRepo.save(contract);
    }

    /**
     * Décode une image de signature en base64 (data URL) et la sauvegarde sur disque.
     * Retourne le chemin de fichier relatif (par ex. "signatures/signature-freelancer-44.png").
     */
    private String saveSignatureImage(Long contractId, String role, String dataUrl) throws java.io.IOException {
        String base64 = dataUrl;
        int commaIdx = dataUrl.indexOf(',');
        if (commaIdx >= 0) {
            base64 = dataUrl.substring(commaIdx + 1);
        }
        byte[] bytes = java.util.Base64.getDecoder().decode(base64);

        java.nio.file.Path dir = java.nio.file.Paths.get("signatures");
        java.nio.file.Files.createDirectories(dir);
        String fileName = "signature-" + role + "-" + contractId + ".png";
        java.nio.file.Path file = dir.resolve(fileName);
        java.nio.file.Files.write(file, bytes);

        System.out.println("✅ [SIGNATURE IMG] Enregistrée : " + file.toAbsolutePath());
        return "signatures/" + fileName;
    }

    // =========================================================================
    // MILESTONES
    // =========================================================================
    @Override
    public ContractMilestone addMilestone(Long contractId, MilestoneRequest request, String authHeader) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        // Autoriser l'ajout de milestones tant que le contrat n'est pas encore signé
        // par le freelancer :
        //   - DRAFT
        //   - ou PENDING_SIGNATURE sans freelancerSignedAt (freelancer n'a pas encore signé)
        boolean canEditMilestones =
            contract.getStatus() == ContractStatus.DRAFT
             || (contract.getStatus() == ContractStatus.PENDING_SIGNATURE
             && contract.getFreelancerSignedAt() == null);

        if (!canEditMilestones) {
            throw new IllegalStateException(
                "Les milestones ne peuvent être ajoutés que avant la signature du freelancer (DRAFT ou PENDING_SIGNATURE avant signature). Statut : "
                    + contract.getStatus());
        }

        ContractMilestone milestone = new ContractMilestone();
        milestone.setTitle(request.getTitle() != null ? request.getTitle() : "");
        milestone.setDescription(request.getDescription());
        milestone.setAmount(request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO);
        milestone.setDeadlineFromString(request.getDeadline());
        milestone.setStatus(MilestoneStatus.PENDING);
        milestone.setOrderIndex(request.getOrderIndex() != null
            ? request.getOrderIndex()
            : (contract.getMilestones().size() + 1));
        milestone.setContract(contract);

        // D'abord persister le milestone pour éviter les erreurs d'instance transiente
        ContractMilestone saved = milestoneRepo.save(milestone);

        // Mettre à jour la collection côté contrat (contexte Hibernate)
        contract.getMilestones().add(saved);

        BigDecimal sum = contract.getMilestones().stream()
            .map(ContractMilestone::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        contract.setTotalAmount(sum);

        contractRepo.save(contract);
        return saved;
    }

    @Override
    public ContractMilestone updateMilestone(Long contractId, Long milestoneId,
                                             MilestoneRequest request, String authHeader) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        ContractMilestone milestone = contract.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Milestone #" + milestoneId + " introuvable"));

        if (request.getTitle()       != null) milestone.setTitle(request.getTitle());
        if (request.getDescription() != null) milestone.setDescription(request.getDescription());
        if (request.getAmount()      != null) milestone.setAmount(request.getAmount());
        if (request.getDeadline()    != null) milestone.setDeadlineFromString(request.getDeadline());
        if (request.getOrderIndex()  != null) milestone.setOrderIndex(request.getOrderIndex());

        BigDecimal sum = contract.getMilestones().stream()
                .map(ContractMilestone::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        contract.setTotalAmount(sum);

        contractRepo.save(contract);
        return milestone;
    }

    @Override
    public void deleteMilestone(Long contractId, Long milestoneId, String authHeader) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        ContractMilestone milestone = contract.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Milestone #" + milestoneId + " introuvable"));

        contract.getMilestones().remove(milestone);

        BigDecimal sum = contract.getMilestones().stream()
                .map(ContractMilestone::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        contract.setTotalAmount(sum);

        contractRepo.save(contract);
    }

    // =========================================================================
    // CLAUSES
    // =========================================================================
    @Override
    public Object updateClause(Long contractId, Long clauseId, Object clauseUpdateRequestObj, String authHeader) {
        Contract contract = contractRepo.findByIdWithClauses(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        ContractClause clauseToUpdate = contract.getClauses().stream()
                .filter(c -> c.getId() != null && c.getId().equals(clauseId))
                .findFirst()
                .orElse(null);

        if (clauseToUpdate == null) {
            initializeDefaultClausesIfNeeded(contract);
            clauseToUpdate = contract.getClauses().stream()
                    .filter(c -> c.getId() != null && c.getId().equals(clauseId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Clause #" + clauseId + " introuvable"));
        }

        String newText = null;
        if (clauseUpdateRequestObj instanceof java.util.Map) {
            Object textObj = ((java.util.Map<?, ?>) clauseUpdateRequestObj).get("text");
            newText = textObj != null ? textObj.toString() : null;
        } else {
            try {
                newText = (String) clauseUpdateRequestObj.getClass().getMethod("getText").invoke(clauseUpdateRequestObj);
            } catch (Exception e) {
                throw new RuntimeException("Impossible d'extraire le texte de la clause", e);
            }
        }

        if (newText == null || newText.trim().isEmpty()) {
            throw new IllegalArgumentException("Le texte de la clause ne peut pas être vide");
        }

        clauseToUpdate.setText(newText);
        clauseToUpdate.setModified(true);
        contractRepo.save(contract);
        return clauseToUpdate;
    }

    // =========================================================================
    // WORKFLOW — accepter proposition
    // =========================================================================
    @Override
    public Contract clientAcceptContract(Long contractId, String authHeader) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        if (contract.getStatus() != ContractStatus.DRAFT) {
            throw new IllegalStateException(
                    "Seuls les contrats en DRAFT peuvent être acceptés. Statut : " + contract.getStatus());
        }

        contract.setStatus(ContractStatus.PENDING_SIGNATURE);

        Long performedBy = contract.getFreelancerId() != null ? contract.getFreelancerId()
                : contract.getClientId() != null ? contract.getClientId()
                : SYSTEM_USER_ID;

        ContractHistory history = new ContractHistory();
        history.setContract(contract);
        history.setAction(ContractAction.SUBMITTED);
        history.setPerformedBy(performedBy);
        history.setOldValue(ContractStatus.DRAFT.name());
        history.setNewValue(ContractStatus.PENDING_SIGNATURE.name());
        historyRepo.save(history);

        Contract saved = contractRepo.save(contract);

        // Notifier le freelancer de signer
        try {
            emailService.sendSignatureRequestToFreelancer(saved, authHeader);
        } catch (Exception e) {
            System.err.println("⚠️ [EMAIL] Erreur notification freelancer : " + e.getMessage());
        }

        return saved;
    }

    // =========================================================================
    // WORKFLOW — envoyer modifications
    // =========================================================================
    @Override
    public Contract sendModificationsToClient(Long contractId, String authHeader) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        if (contract.getStatus() != ContractStatus.DRAFT
            && contract.getStatus() != ContractStatus.PENDING_SIGNATURE) {
            throw new IllegalStateException(
                "Des modifications ne peuvent être envoyées que avant signature (DRAFT ou PENDING_SIGNATURE). Statut : "
                    + contract.getStatus());
        }

        Long performedBy = contract.getFreelancerId() != null ? contract.getFreelancerId() : SYSTEM_USER_ID;

        // Quand le freelancer envoie des modifications, on passe le contrat
        // en DISPUTED pour signaler clairement un désaccord en cours.
        ContractStatus oldStatus = contract.getStatus();
        contract.setStatus(ContractStatus.DISPUTED);

        // Description détaillée des modifications (montants, milestones, etc.)
        String modificationsDesc = buildModificationsDescription(contract);

        // Résumé AI lisible des changements
        String aiSummary;
        try {
            aiSummary = claudeAiService.summarizeModifications(modificationsDesc, "FREELANCER");
        } catch (Exception e) {
            System.err.println("⚠️ [AI] Échec du résumé des modifications : " + e.getMessage());
            aiSummary = "Modifications envoyées au client.";
        }

        ContractHistory history = new ContractHistory();
        history.setContract(contract);
        history.setAction(ContractAction.MODIFICATIONS_SENT);
        history.setPerformedBy(performedBy);
        history.setOldValue("Status: " + oldStatus + " → " + contract.getStatus()
            + "\n" + modificationsDesc);
        history.setNewValue(aiSummary);
        history.setAiSummary(aiSummary);
        historyRepo.save(history);

        // Notifier le client par email (avec la description détaillée)
        try {
            emailService.sendModificationProposalToClient(contract, modificationsDesc, authHeader);
            System.out.println("✅ [EMAIL] Modifications envoyées au client par email");
        } catch (Exception e) {
            System.err.println("⚠️ [EMAIL] Erreur notification modifications : " + e.getMessage());
        }

        return contractRepo.save(contract);
    }

    // =========================================================================
    // WORKFLOW — client accepte modifications
    // =========================================================================
    @Override
    public Contract clientAcceptModifications(Long contractId, String authHeader) {
        Contract contract = contractRepo.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        if (contract.getStatus() != ContractStatus.DISPUTED) {
            throw new IllegalStateException(
                "Le contrat doit être en DISPUTED pour accepter les modifications. Statut : "
                    + contract.getStatus());
        }

        if (contract.getMilestones() == null || contract.getMilestones().isEmpty()) {
            throw new IllegalStateException("Le contrat doit avoir au moins un milestone.");
        }

        BigDecimal milestonesTotal = contract.getMilestones().stream()
                .map(ContractMilestone::getAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (milestonesTotal.compareTo(contract.getTotalAmount()) != 0) {
            throw new IllegalStateException(
                    "La somme des milestones (" + milestonesTotal + ") ≠ montant total ("
                            + contract.getTotalAmount() + ").");
        }

        contract.setStatus(ContractStatus.PENDING_SIGNATURE);

        Long performedBy = contract.getClientId() != null ? contract.getClientId() : SYSTEM_USER_ID;

        ContractHistory history = new ContractHistory();
        history.setContract(contract);
        history.setAction(ContractAction.MODIFICATIONS_ACCEPTED);
        history.setPerformedBy(performedBy);
        history.setOldValue(ContractStatus.DISPUTED.name());
        history.setNewValue(ContractStatus.PENDING_SIGNATURE.name());
        historyRepo.save(history);

        Contract saved = contractRepo.save(contract);

        // Notifier le freelancer par email pour qu'il signe en premier
        try {
            emailService.sendSignatureRequestToFreelancer(saved, authHeader);
            System.out.println("✅ [EMAIL] Notification envoyée au freelancer pour signer");
        } catch (Exception e) {
            System.err.println("⚠️ [EMAIL] Erreur notification freelancer : " + e.getMessage());
        }

        return saved;
    }

    // =========================================================================
    // WORKFLOW — client refuse modifications
    // =========================================================================
    @Override
    public Contract clientRejectModifications(Long contractId, String authHeader) {
        Contract contract = contractRepo.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat #" + contractId + " introuvable"));

        // Le client ne peut refuser que des modifications en cours de litige
        if (contract.getStatus() != ContractStatus.DISPUTED) {
            throw new IllegalStateException(
                "Le contrat doit être en DISPUTED pour refuser les modifications. Statut : "
                            + contract.getStatus());
        }

        // Refus définitif → le contrat est fermé / annulé
        contract.setStatus(ContractStatus.CANCELLED);

        Long performedBy = contract.getClientId() != null ? contract.getClientId() : SYSTEM_USER_ID;

        ContractHistory history = new ContractHistory();
        history.setContract(contract);
        history.setAction(ContractAction.MODIFICATIONS_REJECTED);
        history.setPerformedBy(performedBy);
        history.setOldValue(ContractStatus.DISPUTED.name());
        history.setNewValue(ContractStatus.CANCELLED.name());
        historyRepo.save(history);

        Contract saved = contractRepo.save(contract);

        // Notifier les deux parties du litige
        try {
            emailService.sendDisputeNotification(
                    saved,
                "Le client a refusé les modifications proposées par le freelancer. Le contrat est désormais fermé.",
                    authHeader
            );
        } catch (Exception e) {
            System.err.println("⚠️ [EMAIL] Erreur notification litige : " + e.getMessage());
        }

        return saved;
    }

    // =========================================================================
    // MÉTHODES DE L'INTERFACE — saveContract, notifyXxx, generatePdfAndNotify
    // =========================================================================

    @Override
    public Contract saveContract(Contract contract) {
        return contractRepo.save(contract);
    }

    /**
     * Notifie le client par email que le freelancer a proposé des modifications.
     * Appelé depuis ContractController.sendModificationsToClient()
     */
    @Override
    public void notifyClientOfModifications(Contract contract, String authHeader) {
        try {
            String modificationsDesc = buildModificationsDescription(contract);
            emailService.sendModificationProposalToClient(contract, modificationsDesc, authHeader);
            System.out.println("✅ [EMAIL] Notification modifications envoyée au client");
        } catch (Exception e) {
            System.err.println("⚠️ [EMAIL] Erreur notifyClientOfModifications : " + e.getMessage());
        }
    }

    /**
     * Notifie le freelancer par email pour qu'il signe.
     * Appelé depuis ContractController.clientAcceptModifications()
     */
    @Override
    public void notifyFreelancerToSign(Contract contract, String authHeader) {
        try {
            emailService.sendSignatureRequestToFreelancer(contract, authHeader);
            System.out.println("✅ [EMAIL] Notification de signature envoyée au freelancer");
        } catch (Exception e) {
            System.err.println("⚠️ [EMAIL] Erreur notifyFreelancerToSign : " + e.getMessage());
        }
    }

    /**
     * Génère le PDF et envoie un email aux deux parties avec le PDF en pièce jointe.
     * Appelé depuis ContractController.signContract() quand les deux ont signé.
     */
    @Override
    public void generatePdfAndNotify(Contract contract, String authHeader) {
        // 1. Générer le PDF
        try {
            Map<Long, String> userNames = loadUserNames(authHeader);
            String clientName = resolveUserDisplayName(
                    contract.getClientId(), userNames, authHeader, "Client");
            String freelancerName = resolveUserDisplayName(
                    contract.getFreelancerId(), userNames, authHeader, "Freelancer");

            String pdfPath = pdfGeneratorService.generateContractPdf(
                    contract,
                    clientName,
                    freelancerName,
                    contract.getTitle()
            );
            contract.setPdfUrl(pdfPath);

            ContractHistory pdfHistory = new ContractHistory();
            pdfHistory.setContract(contract);
            pdfHistory.setAction(ContractAction.PDF_GENERATED);
            pdfHistory.setPerformedBy(SYSTEM_USER_ID);
            pdfHistory.setNewValue("PDF généré : " + pdfPath);
            historyRepo.save(pdfHistory);

            System.out.println("✅ [PDF] Généré : " + pdfPath);
        } catch (Exception e) {
            System.err.println("⚠️ [PDF] Échec génération : " + e.getMessage());
        }

        // 2. Sauvegarder avec le pdfUrl mis à jour
        Contract saved = contractRepo.save(contract);

        // 3. ✅ Envoyer emails avec PDF aux deux parties
        try {
            emailService.sendSignedContractToParties(saved, authHeader);
            System.out.println("✅ [EMAIL] Contrat signé envoyé aux deux parties");
        } catch (Exception e) {
            System.err.println("⚠️ [EMAIL] Erreur envoi contrat signé : " + e.getMessage());
        }
    }

    // =========================================================================
    // HELPERS PRIVÉS
    // =========================================================================

    private Map<Long, String> loadUserNames(String authHeader) {
        try {
            if (authHeader == null || authHeader.isBlank()) return Map.of();
            List<Map<String, Object>> users = userServiceClient.getAllUsers(authHeader);
            return users.stream()
                    .filter(u -> u.get("id") != null)
                    .collect(Collectors.toMap(
                            u -> Long.valueOf(u.get("id").toString()),
                            u -> {
                                String fn = u.getOrDefault("firstName", "").toString();
                                String ln = u.getOrDefault("lastName", "").toString();
                                return (fn + " " + ln).trim();
                            },
                            (a, b) -> a
                    ));
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de charger les users : " + e.getMessage());
            return Map.of();
        }
    }

    /**
     * Résout un nom complet à partir d'un userId.
     * 1) tente userNames (chargés via getAllUsers)
     * 2) sinon, appelle userServiceClient.getUserById(id, authHeader)
     * 3) sinon, retourne un fallback "label #id".
     */
    private String resolveUserDisplayName(Long userId, Map<Long, String> userNames,
                                          String authHeader, String labelFallback) {
        if (userId == null) return labelFallback;

        if (userNames != null && userNames.containsKey(userId)) {
            String name = userNames.get(userId);
            if (name != null && !name.isBlank()) return name;
        }

        // Tentative directe via user-service si on a un token
        try {
            if (authHeader != null && !authHeader.isBlank()) {
                Map<String, Object> user = userServiceClient.getUserById(userId, authHeader);
                if (user != null) {
                    String fn = user.getOrDefault("firstName", "").toString();
                    String ln = user.getOrDefault("lastName", "").toString();
                    String full = (fn + " " + ln).trim();
                    if (!full.isBlank()) return full;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de charger l'utilisateur " + userId + " : " + e.getMessage());
        }

        return labelFallback + " #" + userId;
    }

    private ContractResponseDTO toDTO(Contract c, Map<Long, String> userNames) {
        initializeDefaultClausesIfNeeded(c);

        if (c.getId() != null && c.getClauses() != null && !c.getClauses().isEmpty()) {
            boolean hasUnpersisted = c.getClauses().stream().anyMatch(cl -> cl.getId() == null);
            if (hasUnpersisted) {
                List<ContractClause> persisted = ensureDefaultClausesArePersisted(c.getId());
                if (persisted != null && !persisted.isEmpty()) {
                    c.getClauses().clear();
                    c.getClauses().addAll(persisted);
                }
            }
        }

        ContractResponseDTO dto = new ContractResponseDTO();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setProjectId(c.getProjectId());
        dto.setProposalId(c.getProposalId());
        dto.setFreelancerId(c.getFreelancerId());
        dto.setClientId(c.getClientId());
        dto.setTotalAmount(c.getTotalAmount());
        dto.setCurrency(c.getCurrency());
        dto.setStartDate(c.getStartDate());
        dto.setEndDate(c.getEndDate());
        dto.setStatus(c.getStatus() != null ? c.getStatus().name() : "DRAFT");
        dto.setFreelancerSignedAt(c.getFreelancerSignedAt());
        dto.setClientSignedAt(c.getClientSignedAt());
        dto.setPdfUrl(c.getPdfUrl());
        dto.setFreelancerSignatureImagePath(c.getFreelancerSignatureImagePath());
        dto.setClientSignatureImagePath(c.getClientSignatureImagePath());
        dto.setMilestones(c.getMilestones());
        dto.setClauses(c.getClauses());
        dto.setCreatedAt(c.getCreatedAt());

        if (c.getFreelancerId() != null)
            dto.setFreelancerName(userNames.getOrDefault(c.getFreelancerId(), ""));
        if (c.getClientId() != null && !c.getClientId().equals(SYSTEM_USER_ID))
            dto.setClientName(userNames.getOrDefault(c.getClientId(), ""));

        return dto;
    }

    private void initializeDefaultClausesIfNeeded(Contract contract) {
        if (contract == null || contract.getId() == null) return;
        if (contract.getClauses() == null || contract.getClauses().isEmpty()) {
            contract.getClauses().addAll(buildDefaultClauses(contract));
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public List<ContractClause> ensureDefaultClausesArePersisted(Long contractId) {
        try {
            Contract contract = contractRepo.findById(contractId).orElse(null);
            if (contract == null) return new java.util.ArrayList<>();
            if (contract.getClauses() == null || contract.getClauses().isEmpty()) {
                contract.getClauses().addAll(buildDefaultClauses(contract));
                contractRepo.save(contract);
            }
            return contract.getClauses() != null ? contract.getClauses() : new java.util.ArrayList<>();
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de persister les clauses : " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    private void persistDefaultClausesIfNeeded(Contract contract) {
        if (contract == null || contract.getId() == null) return;
        if (contract.getClauses() == null || contract.getClauses().isEmpty()) {
            contract.getClauses().addAll(buildDefaultClauses(contract));
            contractRepo.save(contract);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<ContractHistory> getContractHistory(Long contractId) {
        return historyRepo.findByContractIdOrderByPerformedAtDesc(contractId);
    }

    private List<ContractClause> buildDefaultClauses(Contract contract) {
        String[] articles = {"Art. 1", "Art. 2", "Art. 3", "Art. 4"};
        String[] titles   = {"Intellectual Property", "Confidentiality", "Modifications & Revisions", "Termination"};
        String[] texts    = {
                "All intellectual property rights including code, designs, and assets produced under this contract are transferred to the Client upon full payment of all contract installments.",
                "The Freelancer agrees to maintain strict confidentiality of all information, data, and specifications of the Client for a period of 3 years following the end of this contract.",
                "Minor revision requests (< 2 hours) are included in the agreed price. Major modifications or out-of-scope requests are subject to a separate fee proposal requiring prior written approval from the Client.",
                "In the event of early termination by either party, all payments corresponding to deliverables already completed and accepted remain fully due to the Freelancer."
        };
        List<ContractClause> clauses = new java.util.ArrayList<>();
        for (int i = 0; i < articles.length; i++) {
            ContractClause clause = new ContractClause();
            clause.setArticle(articles[i]);
            clause.setTitle(titles[i]);
            clause.setText(texts[i]);
            clause.setContract(contract);
            clause.setModified(false);
            clauses.add(clause);
        }
        return clauses;
    }

    private String buildModificationsDescription(Contract contract) {
        StringBuilder sb = new StringBuilder();
        sb.append("Le freelancer a proposé des ajustements sur le contrat.");

        if (contract.getMilestones() != null && !contract.getMilestones().isEmpty()) {
            sb.append("<br>• <strong>")
              .append(contract.getMilestones().size())
              .append(" étape(s) de paiement</strong> ont été modifiées :");

            contract.getMilestones().forEach(m -> {
                sb.append("<br>&nbsp;&nbsp;– ")
                  .append(m.getTitle() != null ? m.getTitle() : "Étape sans titre")
                  .append(" : ")
                  .append(m.getAmount() != null ? m.getAmount() : BigDecimal.ZERO)
                  .append(" ")
                  .append(contract.getCurrency() != null ? contract.getCurrency() : "");

                if (m.getDeadline() != null) {
                    sb.append(" (échéance au ").append(m.getDeadline()).append(")");
                }
            });
        }

        return sb.toString();
    }

    private boolean isValidDate(String date) {
        return date != null && !date.isBlank() && date.length() >= 10;
    }

    private String buildFinalDescription(String originalDesc, String aiDesc, List<String> clauses) {
        StringBuilder sb = new StringBuilder();
        String desc = (aiDesc != null && !aiDesc.isBlank()) ? aiDesc : originalDesc;
        if (desc != null && !desc.isBlank()) {
            desc = desc.replace("\"", "'").replace("\\", "/").trim();
            if (desc.length() > 500) desc = desc.substring(0, 500) + "...";
            sb.append(desc).append("\n\n");
        }
        if (clauses != null && !clauses.isEmpty()) {
            sb.append("=== CLAUSES ===\n\n");
            for (String cl : clauses) {
                String cleaned = cl.replace("\"", "'").replace("\\", "/").trim();
                if (cleaned.length() > 300) cleaned = cleaned.substring(0, 300) + "...";
                sb.append(cleaned).append("\n\n");
            }
        }
        return sb.toString().trim();
    }
}