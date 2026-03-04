package tn.esprit.projet_module.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_module.clients.UserDto;
import tn.esprit.projet_module.clients.UserServiceClient;
import tn.esprit.projet_module.entity.*;
import tn.esprit.projet_module.repository.*;
import tn.esprit.projet_module.service.EmailService;
import tn.esprit.projet_module.service.FreelancerInfoService;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/proposals")
public class ProposalController {

    private final EmailService emailService;
    private final ProposalRepository proposalRepo;
    private final ProjectRepository  projectRepo;
    private final FreelancerInfoService freelancerInfoService;  // ← AJOUT

    public ProposalController(ProposalRepository proposalRepo,
                              ProjectRepository projectRepo,
                              EmailService emailService,
                              FreelancerInfoService freelancerInfoService) {  // ← AJOUT
        this.proposalRepo          = proposalRepo;
        this.projectRepo           = projectRepo;
        this.emailService          = emailService;
        this.freelancerInfoService = freelancerInfoService;  // ← AJOUT
    }

    // ── Soumettre une proposition ──
    @PostMapping
    public ResponseEntity<?> submitProposal(@RequestBody Map<String, Object> body) {
        if (body == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Request body is required."));
        }
        Object rawProjectId = body.get("projectId");
        Object rawFreelancerId = body.get("freelancerId");
        Object rawBidAmount = body.get("bidAmount");
        Object rawDeliveryWeeks = body.get("deliveryWeeks");
        if (rawProjectId == null || rawFreelancerId == null || rawBidAmount == null || rawDeliveryWeeks == null) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "Missing required fields: projectId, freelancerId, bidAmount, deliveryWeeks."));
        }
        Long projectId; Long freelancerId; Double bidAmount; Integer deliveryWeeks;
        try {
            projectId = Long.valueOf(rawProjectId.toString());
            freelancerId = Long.valueOf(rawFreelancerId.toString());
            bidAmount = Double.valueOf(rawBidAmount.toString());
            deliveryWeeks = Integer.valueOf(rawDeliveryWeeks.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid number format: " + e.getMessage()));
        }

        if (proposalRepo.existsByProjectIdAndFreelancerId(projectId, freelancerId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You already submitted a proposal for this project."));
        }

        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Proposal p = new Proposal();
        p.setProject(project);
        p.setFreelancerId(freelancerId);
        p.setBidAmount(bidAmount);
        p.setDeliveryWeeks(deliveryWeeks);
        p.setCoverLetter(body.get("coverLetter") != null ? body.get("coverLetter").toString().trim() : "");
        p.setPortfolioUrl(safeString(body.get("portfolioUrl")));
        p.setQuestionToClient(safeString(body.get("questionToClient")));

        if (body.get("freelancerKeycloakId") != null &&
                !body.get("freelancerKeycloakId").toString().trim().isEmpty()) {
            p.setFreelancerKeycloakId(body.get("freelancerKeycloakId").toString().trim());
        }

        Object av = body.get("availableFrom");
        if (av != null && !av.toString().trim().isEmpty()) {
            try {
                p.setAvailableFrom(LocalDate.parse(av.toString().trim()));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid availableFrom date (use yyyy-MM-dd)."));
            }
        }

        proposalRepo.save(p);
        return ResponseEntity.ok(Map.of("message", "Proposal submitted successfully!"));
    }

    private static String safeString(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    // ── Propositions d'un projet (côté client) ──
    // clientId : passé par le frontend (project.clientId ou userId connecté)
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long clientId) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Long ownerId = project.getClientId();
        Long currentClientId = (clientId != null) ? clientId : 1L;
        if (ownerId == null || !ownerId.equals(currentClientId)) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Access denied."));
        }

        List<Proposal> proposals = proposalRepo.findByProjectId(projectId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Proposal prop : proposals) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",               prop.getId());
            m.put("freelancerId",     prop.getFreelancerId());
            m.put("bidAmount",        prop.getBidAmount());
            m.put("deliveryWeeks",    prop.getDeliveryWeeks());
            m.put("availableFrom",    prop.getAvailableFrom());
            m.put("portfolioUrl",     prop.getPortfolioUrl());
            m.put("coverLetter",      prop.getCoverLetter());
            m.put("questionToClient", prop.getQuestionToClient());
            m.put("status",           prop.getStatus());
            m.put("createdAt",        prop.getCreatedAt());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    // ── Propositions d'un freelancer ──
    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<Proposal>> getByFreelancer(@PathVariable Long freelancerId) {
        return ResponseEntity.ok(proposalRepo.findByFreelancerId(freelancerId));
    }

    // ── Vérifier si déjà soumis (par freelancerId) ──
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkProposal(
            @RequestParam Long projectId,
            @RequestParam Long freelancerId) {
        boolean exists = proposalRepo.existsByProjectIdAndFreelancerId(projectId, freelancerId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("hasProposal", exists);
        if (exists) {
            proposalRepo.findByProjectIdAndFreelancerId(projectId, freelancerId)
                    .ifPresent(p -> res.put("status", p.getStatus()));
        }
        return ResponseEntity.ok(res);
    }

    // ── Vérifier si déjà soumis (par keycloakId) ──
    @GetMapping("/check-by-keycloak")
    public ResponseEntity<?> checkByKeycloak(
            @RequestParam Long projectId,
            @RequestParam String keycloakId) {
        boolean exists = proposalRepo
                .existsByProjectIdAndFreelancerKeycloakId(projectId, keycloakId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("hasProposal", exists);
        if (exists) {
            proposalRepo.findByProjectIdAndFreelancerKeycloakId(projectId, keycloakId)
                    .ifPresent(p -> res.put("status", p.getStatus().name()));
        } else {
            res.put("status", "");
        }
        return ResponseEntity.ok(res);
    }

    // ── Compter les propositions d'un projet ──
    @GetMapping("/count/{projectId}")
    public ResponseEntity<Map<String, Object>> countProposals(@PathVariable Long projectId) {
        return ResponseEntity.ok(Map.of("count", proposalRepo.countByProjectId(projectId)));
    }

    // ── Accepter / Rejeter (côté client) ──
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return proposalRepo.findById(id).map(p -> {
            ProposalStatus newStatus = ProposalStatus.valueOf(body.get("status"));
            p.setStatus(newStatus);
            proposalRepo.save(p);

            String projectTitle = p.getProject().getTitle();

            // ── Récupère email depuis Keycloak directement ──
            String freelancerEmail = null;
            String freelancerName  = "Freelancer";

            String keycloakId = p.getFreelancerKeycloakId();
            if (keycloakId != null && !keycloakId.isBlank()) {
                Map<String, String> info = freelancerInfoService.getFreelancerInfo(keycloakId);
                freelancerEmail = info.get("email");
                freelancerName  = info.getOrDefault("fullName", "Freelancer");
            }

            if (freelancerEmail == null || freelancerEmail.isBlank()) {
                System.err.println("⚠️ No email found for keycloakId: " + keycloakId);
                return ResponseEntity.ok(Map.of("message", "Status updated but email not sent."));
            }

            if (newStatus == ProposalStatus.ACCEPTED) {
                emailService.sendProposalAccepted(
                        freelancerEmail, freelancerName, projectTitle, p.getBidAmount()
                );
            } else if (newStatus == ProposalStatus.REJECTED) {
                emailService.sendProposalRejected(
                        freelancerEmail, freelancerName, projectTitle
                );
            }

            System.out.println("📧 Email sent to: " + freelancerEmail);
            return ResponseEntity.ok(Map.of(
                    "message", "Status updated and email sent to " + freelancerEmail
            ));

        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Projets acceptés par freelancerId ──
    @GetMapping("/freelancer/{freelancerId}/accepted-projects")
    public ResponseEntity<List<Map<String, Object>>> getAcceptedProjects(@PathVariable Long freelancerId) {
        List<Proposal> accepted = proposalRepo
                .findByFreelancerIdAndStatus(freelancerId, ProposalStatus.ACCEPTED);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Proposal p : accepted) {
            Project proj = p.getProject();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          proj.getId());
            m.put("title",       proj.getTitle());
            m.put("description", proj.getDescription());
            m.put("deadline",    proj.getDeadline());
            m.put("status",      proj.getStatus() != null ? proj.getStatus().name() : null);
            m.put("clientId",    proj.getClientId());
            m.put("createdAt",   proj.getCreatedAt());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    // ── Projets acceptés par keycloakId ──
    @GetMapping("/freelancer/by-keycloak/{keycloakId}/accepted-projects")
    public ResponseEntity<List<Map<String, Object>>> getAcceptedProjectsByKeycloak(
            @PathVariable String keycloakId) {
        List<Proposal> accepted = proposalRepo
                .findByFreelancerKeycloakIdAndStatus(keycloakId, ProposalStatus.ACCEPTED);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Proposal p : accepted) {
            Project proj = p.getProject();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",          proj.getId());
            m.put("title",       proj.getTitle());
            m.put("description", proj.getDescription());
            m.put("deadline",    proj.getDeadline());
            m.put("status",      proj.getStatus() != null ? proj.getStatus().name() : null);
            m.put("clientId",    proj.getClientId());
            m.put("createdAt",   proj.getCreatedAt());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    // ── Propositions par keycloakId ──
    @GetMapping("/freelancer/keycloak/{keycloakId}")
    public ResponseEntity<List<Proposal>> getByFreelancerKeycloak(
            @PathVariable String keycloakId) {
        return ResponseEntity.ok(proposalRepo.findByFreelancerKeycloakId(keycloakId));
    }
}