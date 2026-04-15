package tn.esprit.matching.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.matching.dto.InvitationDTO;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.service.InvitationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/invitations")
@CrossOrigin(origins = "http://localhost:4200")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @PostMapping("/send")
    public ResponseEntity<Invitation> send(@RequestBody Map<String, Object> body) {
        Long projectId    = Long.valueOf(body.get("projectId").toString());
        Long freelancerId = Long.valueOf(body.get("freelancerId").toString());
        Long clientId     = Long.valueOf(body.get("clientId").toString());
        Double matchScore = Double.valueOf(body.get("matchScore").toString());
        return ResponseEntity.ok(
                invitationService.sendInvitation(projectId, freelancerId, clientId, matchScore));
    }

    // ANCIEN ENDPOINT (on le garde pour l'instant, mais le front n'en fera plus usage)
    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<InvitationDTO>> getForFreelancer(
            @PathVariable Long freelancerId) {
        return ResponseEntity.ok(
                invitationService.getInvitationsForFreelancer(freelancerId));
    }

    // NOUVEAU ENDPOINT pour le freelancer connecté
    @GetMapping("/freelancer/me")
    public ResponseEntity<List<InvitationDTO>> getForCurrentFreelancer(
            @RequestParam Long freelancerId,
            @RequestHeader("Authorization") String token
    ) {
        // Étape 1 : on ignore encore le token, on utilise uniquement freelancerId
        return ResponseEntity.ok(
                invitationService.getInvitationsForFreelancer(freelancerId)
        );
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Invitation> accept(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        return ResponseEntity.ok(invitationService.accept(id, token));
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<Invitation> decline(@PathVariable Long id) {
        return ResponseEntity.ok(invitationService.decline(id));
    }

    @GetMapping("/freelancer/{id}/pending-count")
    public ResponseEntity<Map<String, Long>> pendingCount(@PathVariable Long id) {
        long count = invitationService.getPendingCount(id);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/trash")
    public ResponseEntity<Invitation> trash(@PathVariable Long id) {
        return ResponseEntity.ok(invitationService.trash(id));
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Invitation> restore(@PathVariable Long id) {
        return ResponseEntity.ok(invitationService.restore(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePermanently(@PathVariable Long id) {
        invitationService.deletePermanently(id);
        return ResponseEntity.ok(Map.of("message", "Deleted permanently"));
    }

    @GetMapping("/freelancer/{freelancerId}/trash")
    public ResponseEntity<List<InvitationDTO>> getTrash(
            @PathVariable Long freelancerId) {
        return ResponseEntity.ok(invitationService.getTrash(freelancerId));
    }
}