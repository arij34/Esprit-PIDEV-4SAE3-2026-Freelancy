package tn.esprit.matching.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.matching.dto.AdminInvitationDTO;
import tn.esprit.matching.service.InvitationService;

import java.util.List;

@RestController
@RequestMapping("/admin/invitations")
public class InvitationAdminController {

    @Autowired
    private InvitationService invitationService;

    @GetMapping
    public List<AdminInvitationDTO> getAllInvitations() {
        return invitationService.getAllInvitationsForAdmin();
    }
}