package com.smartfreelance.backend.service;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KeycloakAdminService {

    @Value("${keycloak.admin.base-url}")
    private String baseUrl;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.admin-realm}")
    private String adminRealm;

    @Value("${keycloak.admin.target-realm}")
    private String targetRealm;

    private final RestTemplate restTemplate = new RestTemplate();

    private String cachedToken;
    private Instant cachedTokenExp;

    public String getAdminToken() {
        if (cachedToken != null && cachedTokenExp != null && Instant.now().isBefore(cachedTokenExp.minusSeconds(30))) {
            return cachedToken;
        }

        String tokenUrl = baseUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", "admin-cli");
        form.add("username", adminUsername);
        form.add("password", adminPassword);

        ResponseEntity<Map> resp = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(form, headers), Map.class);
        Map body = resp.getBody();
        if (body == null || body.get("access_token") == null) {
            throw new RuntimeException("Unable to obtain Keycloak admin token");
        }

        cachedToken = body.get("access_token").toString();
        Object expiresIn = body.get("expires_in");
        long seconds = expiresIn instanceof Number ? ((Number) expiresIn).longValue() : 60;
        cachedTokenExp = Instant.now().plusSeconds(seconds);
        return cachedToken;
    }

    public String createUser(String firstName, String lastName, String email, String password) {
        String token = getAdminToken();

        // Create user
        String createUrl = baseUrl + "/admin/realms/" + targetRealm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "username", email,
                "email", email,
                "enabled", true,
                "emailVerified", true,
                "firstName", firstName,
                "lastName", lastName
        );

        ResponseEntity<String> createResp = restTemplate.exchange(createUrl, HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
        URI location = createResp.getHeaders().getLocation();
        String userId;
        if (location != null) {
            String path = location.getPath();
            userId = path.substring(path.lastIndexOf('/') + 1);
        } else {
            // fallback: search by email
            userId = findUserIdByEmail(email);
        }

        // Set password
        String pwdUrl = baseUrl + "/admin/realms/" + targetRealm + "/users/" + userId + "/reset-password";
        Map<String, Object> pwdPayload = Map.of(
                "type", "password",
                "temporary", false,
                "value", password
        );
        restTemplate.exchange(pwdUrl, HttpMethod.PUT, new HttpEntity<>(pwdPayload, headers), String.class);

        return userId;
    }

    public void assignRealmRole(String userId, String roleName) {
        String token = getAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Fetch role representation
        String roleUrl = baseUrl + "/admin/realms/" + targetRealm + "/roles/" + roleName;
        Map roleRep = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class).getBody();
        if (roleRep == null) {
            throw new RuntimeException("Role not found in Keycloak: " + roleName);
        }

        // Assign to user
        String assignUrl = baseUrl + "/admin/realms/" + targetRealm + "/users/" + userId + "/role-mappings/realm";
        restTemplate.exchange(assignUrl, HttpMethod.POST, new HttpEntity<>(List.of(roleRep), headers), String.class);
    }

    public Optional<Map<String, Object>> getUserById(String userId) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String url = baseUrl + "/admin/realms/" + targetRealm + "/users/" + userId;
        try {
            Map body = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class).getBody();
            if (body == null) return Optional.empty();
            return Optional.of((Map<String, Object>) body);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void updateUser(String userId, String firstName, String lastName, String email, boolean enabled) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = baseUrl + "/admin/realms/" + targetRealm + "/users/" + userId;
        // NOTE: Some realms have "Edit username" disabled, making username read-only.
        // So we update email but DO NOT try to update username.
        Map<String, Object> payload = Map.of(
                "firstName", firstName,
                "lastName", lastName,
                "email", email,
                "enabled", enabled
        );
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(payload, headers), String.class);
    }

    public void setUserPassword(String userId, String password) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = baseUrl + "/admin/realms/" + targetRealm + "/users/" + userId + "/reset-password";
        Map<String, Object> payload = Map.of(
                "type", "password",
                "temporary", false,
                "value", password
        );
        restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(payload, headers), String.class);
    }

    public void replaceRealmRoles(String userId, String primaryRole) {
        // Remove known roles, then add the selected one.
        // (Keep default-roles-smart-platform untouched; it's not a realm role mapping here.)
        List<String> known = List.of("ADMIN", "CLIENT", "FREELANCER");
        List<Map<String, Object>> current = getUserRealmRoles(userId).stream()
                .filter(known::contains)
                .map(this::getRealmRoleRep)
                .collect(Collectors.toList());

        if (!current.isEmpty()) {
            removeRealmRoles(userId, current);
        }
        assignRealmRole(userId, primaryRole);
    }

    private Map<String, Object> getRealmRoleRep(String roleName) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String roleUrl = baseUrl + "/admin/realms/" + targetRealm + "/roles/" + roleName;
        Map roleRep = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class).getBody();
        if (roleRep == null) throw new RuntimeException("Role not found in Keycloak: " + roleName);
        return (Map<String, Object>) roleRep;
    }

    private void removeRealmRoles(String userId, List<Map<String, Object>> roleReps) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = baseUrl + "/admin/realms/" + targetRealm + "/users/" + userId + "/role-mappings/realm";
        restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(roleReps, headers), String.class);
    }

    private String findUserIdByEmail(String email) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String searchUrl = baseUrl + "/admin/realms/" + targetRealm + "/users?email=" + urlEncode(email);
        List users = restTemplate.exchange(searchUrl, HttpMethod.GET, new HttpEntity<>(headers), List.class).getBody();
        if (users == null || users.isEmpty()) {
            throw new RuntimeException("Unable to find created user in Keycloak");
        }
        Map u = (Map) users.get(0);
        return u.get("id").toString();
    }

    private String urlEncode(String value) {
        return value.replace(" ", "%20");
    }

    public List<Map<String, Object>> listUsers(int max) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String url = baseUrl + "/admin/realms/" + targetRealm + "/users?max=" + max;
        List list = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class).getBody();
        return (List<Map<String, Object>>) (List<?>) (list != null ? list : List.of());
    }

    public void deleteUser(String userId) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String url = baseUrl + "/admin/realms/" + targetRealm + "/users/" + userId;
        restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
    }

    /**
     * Returns realm roles assigned to a Keycloak user.
     */
    public List<String> getUserRealmRoles(String userId) {
        String token = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String url = baseUrl + "/admin/realms/" + targetRealm + "/users/" + userId + "/role-mappings/realm";
        List list = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class).getBody();
        if (list == null) return List.of();

        return (List<String>) ((List<?>) list).stream()
                .filter(o -> o instanceof Map)
                .map(o -> (Map) o)
                .map(m -> m.get("name"))
                .filter(n -> n != null)
                .map(Object::toString)
                .sorted()
                .collect(Collectors.toList());
    }
}
