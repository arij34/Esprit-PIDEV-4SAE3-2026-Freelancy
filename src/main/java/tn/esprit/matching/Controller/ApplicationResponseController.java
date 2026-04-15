package tn.esprit.matching.Controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.matching.dto.FormResponseRequest;
import tn.esprit.matching.entity.ApplicationResponse;
import tn.esprit.matching.service.ApplicationResponseService;

import java.util.Map;

@RestController
@RequestMapping("/form-response")
@CrossOrigin(origins = "http://localhost:4200")
public class ApplicationResponseController {

    @Autowired
    private ApplicationResponseService applicationResponseService;

    // 🔹 Créer / mettre à jour une réponse (scénario: remplir puis re-modifier)
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody FormResponseRequest request) {
        try {
            ApplicationResponse saved = applicationResponseService.saveResponse(request);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        } catch (IllegalStateException e) {
            // Cas "plus de 24h"
            return ResponseEntity.status(403).body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    // 🔹 Récupérer la réponse existante pour pré-remplir ou review
    @GetMapping("/{invitationId}")
    public ResponseEntity<ApplicationResponse> getByInvitation(@PathVariable Long invitationId) {
        ApplicationResponse resp = applicationResponseService.getByInvitationId(invitationId);
        if (resp == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resp);
    }

    // 🔹 Savoir si le freelancer PEUT encore éditer (<= 24h)
    @GetMapping("/{invitationId}/can-edit")
    public ResponseEntity<Map<String, Boolean>> canEdit(@PathVariable Long invitationId) {
        boolean allowed = applicationResponseService.canEdit(invitationId);
        return ResponseEntity.ok(Map.of("canEdit", allowed));
    }
}