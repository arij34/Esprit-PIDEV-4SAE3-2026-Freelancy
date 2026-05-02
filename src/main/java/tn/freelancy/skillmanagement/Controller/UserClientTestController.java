package tn.freelancy.skillmanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.freelancy.skillmanagement.clients.UserDto;
import tn.freelancy.skillmanagement.clients.UserServiceClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-service")
public class UserClientTestController {

    @Autowired
    private UserServiceClient userServiceClient;

    public UserClientTestController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    /**
     * Test de communication avec le user microservice via Eureka + Feign.
     * Appelle /api/public/ping sur smart-freelance-backend.
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> pingUserService(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Map<String, String> response = userServiceClient.ping();
        return ResponseEntity.ok(response);
    }

    /**
     * Retourne la liste des freelancers.
     *
     * Le backend ne fournit pas d'endpoint /api/users/freelancers,
     * donc on appelle /api/admin/users et on filtre par rôle FREELANCER.
     *
     * ⚠️ IMPORTANT : ce endpoint nécessite un token avec le rôle ADMIN,
     * car /api/admin/users est protégé côté backend.
     * Utilise un token d'un compte ADMIN pour appeler cet endpoint.
     */
    @GetMapping("/freelancers")
    public ResponseEntity<?> getFreelancers(
            @RequestHeader("Authorization") String token) {

        try {
            // Récupérer tous les users depuis /api/admin/users
            List<Map<String, Object>> allUsers = userServiceClient.getAllUsers(token);

            // Filtrer uniquement les FREELANCER
            List<UserDto> freelancers = allUsers.stream()
                    .filter(u -> {
                        // Vérifier dans keycloakRoles (source principale)
                        Object kcRoles = u.get("keycloakRoles");
                        if (kcRoles instanceof List<?> roleList) {
                            if (roleList.contains("FREELANCER")) return true;
                        }
                        // Fallback : vérifier dbRole
                        return "FREELANCER".equals(u.get("dbRole"));
                    })
                    .map(u -> {
                        // id peut être null si l'user n'existe pas encore en DB locale
                        Long id = null;
                        if (u.get("dbId") != null) {
                            id = Long.valueOf(u.get("dbId").toString());
                        }
                        String firstName = u.get("firstName") != null ? u.get("firstName").toString() : "";
                        String lastName  = u.get("lastName")  != null ? u.get("lastName").toString()  : "";
                        String email     = u.get("email")     != null ? u.get("email").toString()     : "";
                        String role      = u.get("dbRole")    != null ? u.get("dbRole").toString()    : "FREELANCER";
                        boolean enabled  = u.get("enabled") instanceof Boolean b ? b : true;

                        return new UserDto(id, firstName, lastName, email, role, enabled);
                    })
                    .toList();

            return ResponseEntity.ok(freelancers);

        } catch (feign.FeignException.Forbidden | feign.FeignException.Unauthorized e) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "Token insuffisant : le rôle ADMIN est requis pour accéder à la liste des users.")
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}