package tn.esprit.matching.Controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.matching.dto.ClientInvitationDTO;
import tn.esprit.matching.service.ClientInvitationService;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectInvitationsController {

    private final ClientInvitationService clientInvitationService;

    public ProjectInvitationsController(ClientInvitationService clientInvitationService) {
        this.clientInvitationService = clientInvitationService;
    }

    // GET /projects/{projectId}/invitations
    @GetMapping("/{projectId}/invitations")
    public List<ClientInvitationDTO> getInvitationsForProject(@PathVariable Long projectId) {
        return clientInvitationService.getInvitationsForProject(projectId);
    }
}