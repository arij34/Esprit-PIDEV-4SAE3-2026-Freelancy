package com.smartfreelance.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartfreelance.backend.model.User;
import com.smartfreelance.backend.repository.UserRepository;
import com.smartfreelance.backend.service.KeycloakAdminService;

/**
 * Public signup endpoint.
 * Creates the user in Keycloak (with selected realm role) and stores a local DB record with role=NULL.
 */
@RestController
@RequestMapping("/api/public")
public class PublicSignupController {

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    @Autowired
    private UserRepository userRepository;

    public record SignupRequest(String firstName, String lastName, String email, String password, String accountType) {
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        if (req == null || isBlank(req.email) || isBlank(req.password) || isBlank(req.firstName) || isBlank(req.lastName)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }
        if (userRepository.existsByEmail(req.email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        String role = null;
        if (!isBlank(req.accountType)) {
            String upper = req.accountType.toUpperCase();
            if (upper.equals("CLIENT") || upper.equals("FREELANCER")) {
                role = upper;
            }
        }

        // Create in Keycloak + assign realm role
        String keycloakId = keycloakAdminService.createUser(req.firstName, req.lastName, req.email, req.password);
        if (role != null) {
            keycloakAdminService.assignRealmRole(keycloakId, role);
        }

        // Send email verification link via Keycloak
        try {
            keycloakAdminService.sendVerifyEmail(keycloakId);
        } catch (Exception e) {
            // If SMTP isn't configured, we don't want to rollback user creation.
            // The admin can re-send verification emails from Keycloak UI.
            System.out.println("WARN: Failed to send verification email: " + e.getMessage());
        }

        // Create in local DB with role = null (will be filled on sign-in via /api/me/sync)
        User user = new User();
        user.setFirstName(req.firstName);
        user.setLastName(req.lastName);
        user.setEmail(req.email);
        user.setKeycloakId(keycloakId);
        // DB currently has password NOT NULL, but password is managed by Keycloak.
        // Keep an empty string to satisfy the constraint.
        user.setPassword("");
        user.setRole(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message",
                "Signup successful. Please check your email to confirm your account before signing in."
        ));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
