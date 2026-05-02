package tn.esprit.challengeservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void jwtDecoder_shouldCreateDecoder() {
        JwtDecoder decoder = securityConfig.jwtDecoder("http://localhost:8080/realms/test/protocol/openid-connect/certs");

        assertNotNull(decoder);
    }

    @Test
    void keycloakJwtAuthenticationConverter_shouldUseKeycloakConverter() {
        JwtAuthenticationConverter converter = securityConfig.keycloakJwtAuthenticationConverter();

        Object delegate = ReflectionTestUtils.getField(converter, "jwtGrantedAuthoritiesConverter");
        assertNotNull(delegate);
        assertTrue(delegate instanceof KeycloakRealmRoleConverter);
    }

    @Test
    void corsConfigurationSource_shouldExposeExpectedRules() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration cfg = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/test"));

        assertNotNull(cfg);
        assertEquals(1, cfg.getAllowedOrigins().size());
        assertEquals("http://localhost:4200", cfg.getAllowedOrigins().get(0));
        assertTrue(cfg.getAllowedMethods().contains("GET"));
        assertTrue(cfg.getAllowedMethods().contains("PATCH"));
        assertEquals(Boolean.TRUE, cfg.getAllowCredentials());
    }
}
