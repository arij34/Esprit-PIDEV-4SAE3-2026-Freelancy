package com.smartfreelance.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartfreelance.backend.controller.dto.AdminCreateUserRequest;
import com.smartfreelance.backend.controller.dto.AdminUpdateUserRequest;
import com.smartfreelance.backend.model.User;
import com.smartfreelance.backend.repository.UserRepository;
import com.smartfreelance.backend.service.KeycloakAdminService;

/**
 * Admin-only endpoints for managing users.
 * NOTE: Protected by SecurityConfig: /api/admin/** requires ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUsersController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakAdminService keycloak;

    @GetMapping
    public List<UserRow> list() {
        // Keycloak is the source of truth for the list (as requested).
        // We still enrich with DB role if user exists locally.
        List<Map<String, Object>> kcUsers = keycloak.listUsers(200);
        return kcUsers.stream().map(kc -> {
            String keycloakId = (String) kc.get("id");
            String email = kc.get("email") != null ? kc.get("email").toString() : null;
            String firstName = kc.get("firstName") != null ? kc.get("firstName").toString() : "";
            String lastName = kc.get("lastName") != null ? kc.get("lastName").toString() : "";
            boolean enabled = kc.get("enabled") instanceof Boolean b ? b : true;

            // DB enrichment
            String dbRole = null;
            if (keycloakId != null) {
                Optional<User> u = userRepository.findByKeycloakId(keycloakId);
                if (u.isPresent() && u.get().getRole() != null) {
                    dbRole = u.get().getRole().name();
                }
            }

            // Keycloak realm roles
            List<String> kcRoles = List.of();
            if (keycloakId != null) {
                try {
                    kcRoles = keycloak.getUserRealmRoles(keycloakId);
                } catch (Exception ignored) {
                }
            }

            return new UserRow(null, firstName, lastName, email, dbRole, enabled, keycloakId, kcRoles);
        }).toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AdminCreateUserRequest req) {
        if (req == null || req.email() == null || req.password() == null || req.role() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email/password/role required"));
        }
        String firstName = req.firstName() != null ? req.firstName() : "";
        String lastName = req.lastName() != null ? req.lastName() : "";

        String userId = keycloak.createUser(firstName, lastName, req.email(), req.password());
        keycloak.replaceRealmRoles(userId, req.role());

        return ResponseEntity.ok(Map.of("keycloakId", userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody AdminUpdateUserRequest req) {
        if (req == null) return ResponseEntity.badRequest().body(Map.of("error", "body required"));

        var existingOpt = keycloak.getUserById(id);
        if (existingOpt.isEmpty()) return ResponseEntity.notFound().build();
        Map<String, Object> existing = existingOpt.get();

        String firstName = req.firstName() != null ? req.firstName() : (existing.get("firstName") != null ? existing.get("firstName").toString() : "");
        String lastName = req.lastName() != null ? req.lastName() : (existing.get("lastName") != null ? existing.get("lastName").toString() : "");
        String email = req.email() != null ? req.email() : (existing.get("email") != null ? existing.get("email").toString() : "");
        boolean enabled = req.enabled() != null ? req.enabled() : (existing.get("enabled") instanceof Boolean b ? b : true);

        keycloak.updateUser(id, firstName, lastName, email, enabled);
        if (req.password() != null && !req.password().isBlank()) {
            keycloak.setUserPassword(id, req.password());
        }
        if (req.role() != null && !req.role().isBlank()) {
            keycloak.replaceRealmRoles(id, req.role());
        }

        // update DB profile email if exists
        userRepository.findByKeycloakId(id).ifPresent(u -> {
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setEmail(email);
            u.setEnabled(enabled);
            userRepository.save(u);
        });

        return ResponseEntity.ok(Map.of("message", "updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        // delete in Keycloak
        try {
            keycloak.deleteUser(id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }

        // delete from DB if exists
        userRepository.findByKeycloakId(id).ifPresent(userRepository::delete);

        return ResponseEntity.ok(Map.of("message", "deleted"));
    }

    public record UserRow(Long dbId, String firstName, String lastName, String email, String dbRole, boolean enabled, String keycloakId, List<String> keycloakRoles) {}
}
