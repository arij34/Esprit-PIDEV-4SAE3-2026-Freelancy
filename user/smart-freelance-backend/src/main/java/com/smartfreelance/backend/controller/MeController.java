package com.smartfreelance.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartfreelance.backend.model.Role;
import com.smartfreelance.backend.model.User;
import com.smartfreelance.backend.repository.UserRepository;
import com.smartfreelance.backend.service.KeycloakAdminService;

/**
 * Hybrid auth support: users authenticate in Keycloak, and we keep a local DB user profile.
 *
 * Frontend calls POST /api/me/sync after login to create/update the DB record.
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakAdminService keycloakAdmin;

    @PostMapping("/sync")
    public ResponseEntity<UserDto> sync(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        String keycloakId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String givenName = firstNonBlank(jwt.getClaimAsString("given_name"), jwt.getClaimAsString("name"));
        String familyName = jwt.getClaimAsString("family_name");

        // fallback: split "name" if needed
        if (isBlank(familyName)) {
            String fullName = jwt.getClaimAsString("name");
            if (!isBlank(fullName) && fullName.contains(" ")) {
                String[] parts = fullName.split(" ", 2);
                givenName = firstNonBlank(givenName, parts[0]);
                familyName = parts[1];
            }
        }

        if (isBlank(email)) {
            // email is required by our DB schema
            return ResponseEntity.badRequest().build();
        }

        Role role = mapRoleFromRealmAccess(jwt);

        Optional<User> existing = userRepository.findByKeycloakId(keycloakId);
        User user = existing.orElseGet(() -> userRepository.findByEmail(email));
        if (user == null) {
            user = new User();
            user.setEmail(email);
        }

        user.setKeycloakId(keycloakId);
        user.setFirstName(firstNonBlank(user.getFirstName(), givenName, jwt.getClaimAsString("preferred_username"), email));
        // lastName is NOT NULL in DB; use a safe default
        user.setLastName(firstNonBlank(user.getLastName(), familyName, "-"));
        // Fill DB role on sign-in from Keycloak roles (only if DB role is missing)
        if (user.getRole() == null && role != null) {
            user.setRole(role);
        }

        // DB currently has password NOT NULL, but password is managed by Keycloak.
        // Keep an empty string to satisfy the constraint.
        user.setPassword("");

        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserDto.from(saved));
    }

    public record SetRoleRequest(String role) {}

    /**
     * Assign a primary realm role (CLIENT/FREELANCER) to the currently authenticated user.
     * This is mainly used after the first social login (Google) when the user has no realm role yet.
     */
    @PostMapping("/role")
    public ResponseEntity<?> setRole(@AuthenticationPrincipal Jwt jwt, @RequestBody SetRoleRequest req) {
        if (jwt == null) return ResponseEntity.status(401).build();
        if (req == null || isBlank(req.role())) return ResponseEntity.badRequest().body(Map.of("error", "role required"));

        String role = req.role().toUpperCase();
        if (!(role.equals("CLIENT") || role.equals("FREELANCER"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "role must be CLIENT or FREELANCER"));
        }

        String keycloakId = jwt.getSubject();
        try {
            keycloakAdmin.replaceRealmRoles(keycloakId, role);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Keycloak role assignment failed: " + e.getMessage()));
        }

        // Also persist in local DB (best-effort)
        try {
            Role r = Role.valueOf(role);
            userRepository.findByKeycloakId(keycloakId).ifPresent(u -> {
                u.setRole(r);
                userRepository.save(u);
            });
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok(Map.of("message", "role assigned", "role", role));
    }

    @GetMapping
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.status(401).build();
        String keycloakId = jwt.getSubject();
        return userRepository.findByKeycloakId(keycloakId)
                .map(u -> ResponseEntity.ok(UserDto.from(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record UpdateMeRequest(String firstName, String lastName, String email, String password) {}

    /**
     * Updates current user profile in local DB + Keycloak account (email/username + password).
     * Requires auth.
     */
    @PutMapping
    public ResponseEntity<?> update(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateMeRequest req) {
        if (jwt == null) return ResponseEntity.status(401).build();
        if (req == null) return ResponseEntity.badRequest().body(Map.of("error", "body required"));

        String keycloakId = jwt.getSubject();
        Optional<User> existing = userRepository.findByKeycloakId(keycloakId);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        User user = existing.get();

        String firstName = !isBlank(req.firstName()) ? req.firstName() : user.getFirstName();
        String lastName = !isBlank(req.lastName()) ? req.lastName() : user.getLastName();
        String email = !isBlank(req.email()) ? req.email() : user.getEmail();

        // Update Keycloak account as well (so changes persist on next login)
        try {
            boolean enabled = user.isEnabled();
            keycloakAdmin.updateUser(keycloakId, firstName, lastName, email, enabled);
            if (!isBlank(req.password())) {
                keycloakAdmin.setUserPassword(keycloakId, req.password());
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Keycloak update failed: " + e.getMessage()));
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        // password is managed by Keycloak; keep empty string for DB constraint
        user.setPassword("");
        userRepository.save(user);

        return ResponseEntity.ok(UserDto.from(user));
    }

    private Role mapRoleFromRealmAccess(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) return null;
            Object rolesObj = realmAccess.get("roles");
            if (!(rolesObj instanceof List<?> rolesList)) return null;

            // Priority: ADMIN > CLIENT > FREELANCER
            if (rolesList.contains("ADMIN")) return Role.ADMIN;
            if (rolesList.contains("CLIENT")) return Role.CLIENT;
            if (rolesList.contains("FREELANCER")) return Role.FREELANCER;
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (!isBlank(v)) return v;
        }
        return null;
    }

    public record UserDto(Long id, String firstName, String lastName, String email, String role, boolean enabled) {
        static UserDto from(User u) {
            return new UserDto(
                    u.getId(),
                    u.getFirstName(),
                    u.getLastName(),
                    u.getEmail(),
                    u.getRole() != null ? u.getRole().name() : null,
                    u.isEnabled()
            );
        }
    }
}
