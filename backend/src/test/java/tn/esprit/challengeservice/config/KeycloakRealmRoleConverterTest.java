package tn.esprit.challengeservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void convert_shouldExtractRealmAndClientRoles() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("ADMIN", "FREELANCER")))
                .claim("resource_access", Map.of(
                        "challenge-service", Map.of("roles", List.of("ROLE_USER", "MANAGER"))
                ))
                .build();

        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        Set<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_FREELANCER"));
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_MANAGER"));
    }

    @Test
    void convert_whenClaimsMissing_shouldReturnEmpty() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-1")
                .build();

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void convert_shouldDeduplicateRoles() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("ADMIN", "ADMIN", "ROLE_ADMIN")))
                .build();

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }
}
