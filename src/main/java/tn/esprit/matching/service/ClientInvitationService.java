package tn.esprit.matching.service;

import org.springframework.stereotype.Service;
import tn.esprit.matching.dto.ClientInvitationDTO;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.repository.InvitationRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ClientInvitationService {

    private final InvitationRepository invitationRepository;

    public ClientInvitationService(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    public List<ClientInvitationDTO> getInvitationsForProject(Long projectId) {
        List<Invitation> invitations = invitationRepository.findByProjectId(projectId);

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return invitations.stream().map(inv -> {
            ClientInvitationDTO dto = new ClientInvitationDTO();

            dto.setId(inv.getId());
            dto.setProjectId(inv.getProjectId());

            // Titre du projet (si tu l'as dans Invitation, sinon texte par défaut)
            if (inv.getProjectTitle() != null) {
                dto.setProjectTitle(inv.getProjectTitle());
            } else {
                dto.setProjectTitle("Project " + inv.getProjectId());
            }

            // Nom du freelancer : lu dans l'entité Invitation (freelancerFullName)
            dto.setFreelancerName(inv.getFreelancerFullName());

            dto.setStatus(inv.getStatus().name());

            if (inv.getCreatedAt() != null) {
                dto.setInvitedAt(inv.getCreatedAt().format(fmt));
            }

            if (inv.getRespondedAt() != null) {
                dto.setRespondedAt(inv.getRespondedAt().format(fmt));
            }

            return dto;
        }).toList();
    }
}