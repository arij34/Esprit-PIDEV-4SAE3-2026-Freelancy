package tn.esprit.contrat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * DocuSign API Configuration
 */
@Configuration
public class DocuSignConfig {

    @Value("${docusign.base-path:https://demo.docusign.net/restapi}")
    private String basePath;

    @Value("${docusign.account-id}")
    private String accountId;

    @Value("${docusign.client-id}")
    private String clientId;

    @Value("${docusign.client-secret}")
    private String clientSecret;

    @Value("${docusign.impersonated-user-id}")
    private String impersonatedUserId;

    @Value("${docusign.return-url:http://localhost:4200/contracts}")
    private String returnUrl;

    /**
     * ✅ RestTemplate spécifique pour DocuSign
     */
    @Bean
    public RestTemplate docusignRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    // Getters
    public String getBasePath() { return basePath; }
    public String getAccountId() { return accountId; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getImpersonatedUserId() { return impersonatedUserId; }
    public String getReturnUrl() { return returnUrl; }
}