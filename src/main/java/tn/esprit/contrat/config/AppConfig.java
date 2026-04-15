package tn.esprit.contrat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration Spring pour le RestTemplate.
 * Utilisé par EmailService pour récupérer les informations des utilisateurs
 * (email, nom) depuis l'user-service avant d'envoyer les emails.
 */
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
