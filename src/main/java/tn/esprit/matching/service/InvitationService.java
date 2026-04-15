package tn.esprit.matching.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.matching.clients.InvitationProjectDTO;
import tn.esprit.matching.clients.ProjectClient;
import tn.esprit.matching.dto.AdminInvitationDTO;
import tn.esprit.matching.dto.InvitationDTO;
import tn.esprit.matching.entity.ApplicationResponse;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.entity.InvitationStatus;
import tn.esprit.matching.entity.Matching;
import tn.esprit.matching.repository.ApplicationResponseRepository;
import tn.esprit.matching.repository.InvitationRepository;
import tn.esprit.matching.repository.MatchingRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InvitationService {

    @Autowired
    private UserContextService userContextService;
    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private ProjectClient projectClient;

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private ApplicationResponseRepository applicationResponseRepository;

    public Invitation sendInvitation(Long projectId, Long freelancerId,
                                     Long clientId, Double matchScore) {
        if (invitationRepository.existsByProjectIdAndFreelancerId(projectId, freelancerId)) {
            throw new RuntimeException("Invitation already sent");
        }
        Invitation inv = new Invitation();
        inv.setProjectId(projectId);
        inv.setFreelancerId(freelancerId);
        inv.setClientId(clientId);
        inv.setMatchScore(matchScore);
        inv.setStatus(InvitationStatus.PENDING);
        return invitationRepository.save(inv);
    }

    // ✅ TRASH exclu de la liste principale
    public List<InvitationDTO> getInvitationsForFreelancer(Long freelancerId) {
        List<Invitation> invitations = invitationRepository.findByFreelancerId(freelancerId);

        List<InvitationDTO> dtos = new ArrayList<>();

        for (Invitation inv : invitations) {
            try {
                InvitationProjectDTO projectDto =
                        projectClient.getInvitationData(inv.getProjectId());

                InvitationDTO dto = new InvitationDTO();
                dto.setId(inv.getId());
                dto.setProjectId(inv.getProjectId());

                if (projectDto != null) {
                    dto.setProjectTitle(projectDto.getTitle());
                    dto.setProjectDescription(projectDto.getDescription());
                    // 🔹 nom + email du client
                    dto.setClientName(projectDto.getClientName());
                    dto.setClientEmail(projectDto.getClientEmail());
                    dto.setDeadline(projectDto.getDeadline());
                    dto.setBudgetMin(projectDto.getBudgetMin());
                    dto.setBudgetMax(projectDto.getBudgetMax());
                    dto.setBudgetRecommended(projectDto.getBudgetRecommended());
                    dto.setDurationEstimatedWeeks(projectDto.getDurationEstimatedWeeks());
                    dto.setRequiredSkills(projectDto.getRequiredSkills());
                }

                dto.setStatus(inv.getStatus().name());
                dto.setInvitedAt(inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : null);
                dto.setMatchScore(inv.getMatchScore()); // si tu stockes le score sur l’invitation

                dto.setTrashedAt(inv.getTrashedAt());

                dtos.add(dto);
            } catch (Exception e) {
                System.err.println("Erreur construction InvitationDTO pour invitation " + inv.getId()
                        + " : " + e.getMessage());
            }
        }

        return dtos;
    }

    public Invitation accept(Long invitationId, String token) {
        Invitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        if (inv.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Invalid status transition");
        }

        inv.setStatus(InvitationStatus.ACCEPTED);
        inv.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(inv);

        try {
            // 🔹 1) Récupérer le Keycloak ID (sub) à partir du token
            String freelancerKeycloakId = userContextService.getCurrentKeycloakSub(token);

            // 🔹 2) Récupérer les réponses du formulaire (ton code existant)
            ApplicationResponse app = applicationResponseRepository
                    .findFirstByInvitationId(invitationId);

            String coverLetter = "";
            String questionToClient = "";
            Double bidAmount = 0.0;
            Integer deliveryWeeks = 0;

            if (app != null) {
                coverLetter = app.getAnswerQ1() != null ? app.getAnswerQ1().trim() : "";

                if (app.getAnswerQ3() != null) {
                    String q3 = app.getAnswerQ3().toLowerCase();
                    String digits = q3.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) {
                        try {
                            deliveryWeeks = Integer.parseInt(digits);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (deliveryWeeks == 0) deliveryWeeks = 3;

                if (app.getAnswerQ4() != null && !app.getAnswerQ4().trim().isEmpty()) {
                    try {
                        bidAmount = Double.parseDouble(app.getAnswerQ4().trim());
                    } catch (NumberFormatException e) {
                        bidAmount = 0.0;
                    }
                }

                questionToClient = app.getAnswerQ5() != null ? app.getAnswerQ5().trim() : "";
            } else {
                deliveryWeeks = 3;
                bidAmount = 0.0;
            }

            // 🔹 3) Créer une proposal dans le module projet
            Map<String, Object> body = new HashMap<>();
            body.put("projectId", inv.getProjectId());
            body.put("freelancerId", inv.getFreelancerId());
            body.put("bidAmount", bidAmount);
            body.put("deliveryWeeks", deliveryWeeks);
            body.put("coverLetter", coverLetter);
            body.put("questionToClient", questionToClient);
            body.put("freelancerKeycloakId", freelancerKeycloakId); // ✅ NOUVEAU

            projectClient.createProposalFromMatching(body);

        } catch (Exception e) {
            System.err.println("Erreur création proposal depuis matching: " + e.getMessage());
        }

        try {
            // 🔹 4) Mettre à jour le statut pour déclencher l'email (ton code existant)
            Map<String, Object> proposalInfo =
                    projectClient.getProposalByProjectAndFreelancer(inv.getProjectId(), inv.getFreelancerId());

            Object idObj = proposalInfo.get("id");
            if (idObj != null) {
                Long proposalId = Long.valueOf(idObj.toString());

                Map<String, String> statusBody = new HashMap<>();
                statusBody.put("status", "ACCEPTED");
                projectClient.updateProposalStatus(proposalId, statusBody);
            } else {
                System.err.println("Impossible de récupérer id de proposal pour email");
            }

        } catch (Exception e) {
            System.err.println("Erreur mise à jour statut / envoi email depuis matching: " + e.getMessage());
        }

        return inv;
    }
    public Invitation decline(Long invitationId) {
        Invitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));
        if (inv.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Invalid status transition");
        }
        inv.setStatus(InvitationStatus.DECLINED);
        inv.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(inv);
        try {
            Matching matching = matchingRepository
                    .findByFreelancerIdAndProjectId(inv.getFreelancerId(), inv.getProjectId());
            if (matching != null) {
                matching.setStatus("DECLINED");
                matchingRepository.save(matching);
            }
        } catch (Exception e) {
            System.err.println("Erreur mise à jour matching: " + e.getMessage());
        }
        return inv;
    }

    public long getPendingCount(Long freelancerId) {
        return invitationRepository
                .countByFreelancerIdAndStatus(freelancerId, InvitationStatus.PENDING);
    }

    // ✅ Accepte DECLINED et PENDING
    public Invitation trash(Long invitationId) {
        Invitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        System.out.println("=== TRASH === id=" + invitationId
                + " | status=" + inv.getStatus());

        if (inv.getStatus() != InvitationStatus.DECLINED
                && inv.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException(
                    "Cannot trash invitation with status: " + inv.getStatus());
        }

        inv.setStatus(InvitationStatus.TRASH);
        inv.setTrashedAt(LocalDateTime.now());
        return invitationRepository.save(inv);
    }

    public Invitation restore(Long invitationId) {
        Invitation inv = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        // ✅ Accepte TRASH et DECLINED
        if (inv.getStatus() != InvitationStatus.TRASH
                && inv.getStatus() != InvitationStatus.DECLINED) {
            throw new RuntimeException("Cannot restore from status: " + inv.getStatus());
        }

        inv.setStatus(InvitationStatus.PENDING);
        inv.setTrashedAt(null);
        inv.setRespondedAt(null);
        return invitationRepository.save(inv);
    }

    public void deletePermanently(Long invitationId) {
        invitationRepository.deleteById(invitationId);
    }

    // ✅ Corbeille enrichie avec données projet
    public List<InvitationDTO> getTrash(Long freelancerId) {
        List<Invitation> trashed = invitationRepository
                .findByFreelancerIdAndStatus(freelancerId, InvitationStatus.TRASH);

        return trashed.stream().map(inv -> {
            InvitationDTO dto = new InvitationDTO();
            dto.setId(inv.getId());
            dto.setProjectId(inv.getProjectId());
            dto.setMatchScore(inv.getMatchScore());
            dto.setStatus(inv.getStatus().name());
            dto.setTrashedAt(inv.getTrashedAt());
            dto.setInvitedAt(
                    inv.getCreatedAt() != null
                            ? inv.getCreatedAt().toLocalDate().toString()
                            : null
            );
            try {
                InvitationProjectDTO project =
                        projectClient.getInvitationData(inv.getProjectId());
                if (project != null) {
                    dto.setProjectTitle(project.getTitle());
                    dto.setProjectDescription(project.getDescription());
                    dto.setClientName(project.getClientName());
                    dto.setDeadline(project.getDeadline());
                    dto.setBudgetMin(project.getBudgetMin());
                    dto.setBudgetMax(project.getBudgetMax());
                    dto.setBudgetRecommended(project.getBudgetRecommended());
                    dto.setDurationEstimatedWeeks(project.getDurationEstimatedWeeks());
                    dto.setRequiredSkills(
                            project.getRequiredSkills() != null
                                    ? project.getRequiredSkills()
                                    : Collections.emptyList()
                    );
                }
            } catch (Exception e) {
                System.err.println("Erreur projet corbeille ID = " + inv.getProjectId());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 10000) // toutes les 10 secondes
    public void autoDeleteTrashed() {
        LocalDateTime limit = LocalDateTime.now().minusSeconds(30); // ← 24h → 30s pour test

        System.out.println("Now   : " + LocalDateTime.now());
        System.out.println("Limit : " + limit);

        List<Invitation> toDelete = invitationRepository
                .findByStatusAndTrashedAtBefore(InvitationStatus.TRASH, limit);

        System.out.println("Found to delete: " + toDelete.size());
        invitationRepository.deleteAll(toDelete);
        System.out.println("Auto-deleted " + toDelete.size() + " trashed invitations");
    }

    public List<AdminInvitationDTO> getAllInvitationsForAdmin() {
        List<Invitation> invitations = invitationRepository.findAllByOrderByCreatedAtDesc();

        List<AdminInvitationDTO> dtos = new ArrayList<>();

        for (Invitation inv : invitations) {
            AdminInvitationDTO dto = new AdminInvitationDTO();
            dto.setId(inv.getId());
            dto.setProjectId(inv.getProjectId());
            dto.setFreelancerId(inv.getFreelancerId());
            dto.setStatus(inv.getStatus().name());
            dto.setTrashedAt(inv.getTrashedAt());
            dtos.add(dto);
        }

        return dtos;
    }

}