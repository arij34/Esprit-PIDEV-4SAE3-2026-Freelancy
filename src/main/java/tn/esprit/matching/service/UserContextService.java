package tn.esprit.matching.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class UserContextService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extrait le "sub" (ID Keycloak) à partir du token JWT passé dans le header Authorization.
     * Header attendu: "Bearer eyJhbGciOi..."
     */
    public String getCurrentKeycloakSub(String tokenHeader) {
        if (tokenHeader == null || tokenHeader.isBlank()) {
            return null;
        }
        try {
            String token = tokenHeader.replace("Bearer ", "").trim();
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            String payloadJson = new String(
                    Base64.getUrlDecoder().decode(parts[1]),
                    StandardCharsets.UTF_8
            );
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);

            Object sub = payload.get("sub");
            return sub != null ? sub.toString() : null;

        } catch (Exception e) {
            System.err.println("Erreur parsing token JWT: " + e.getMessage());
            return null;
        }
    }
}