package tn.esprit.contrat.service;

import org.springframework.stereotype.Service;
import tn.esprit.contrat.clients.UserDto;
import tn.esprit.contrat.clients.UserServiceClient;

@Service
public class UserContextService {

    private final UserServiceClient userServiceClient;

    public UserContextService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    /**
     * Résout l'utilisateur courant depuis le user service via le Bearer token.
     * Le user service valide le JWT Keycloak et retourne le profil utilisateur.
     *
     * @param authHeader "Bearer <token>" ou null
     * @return UserDto si le token est valide, null sinon
     */
    public UserDto resolveCurrentUser(String authHeader) {
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            return userServiceClient.getCurrentUser(authHeader);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * S'assure que le header contient bien le préfixe "Bearer ".
     */
    public String ensureBearer(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) return null;
        return authHeader.startsWith("Bearer ") ? authHeader : "Bearer " + authHeader;
    }
}
