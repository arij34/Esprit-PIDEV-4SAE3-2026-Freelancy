package tn.esprit.contrat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import tn.esprit.contrat.repository.ContractRepository;
import tn.esprit.contrat.service.DocuSignServiceImpl;
import tn.esprit.contrat.service.IDocuSignService;
import tn.esprit.contrat.service.MockDocuSignService;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Logger;

@Configuration
public class DocuSignServiceConfiguration {

    private static final Logger logger = Logger.getLogger(DocuSignServiceConfiguration.class.getName());

    @Bean
    @Primary
    public IDocuSignService docuSignService(
            DocuSignConfig docuSignConfig,
            ContractRepository contractRepository,
            @Qualifier("docusignRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper) {

        if (isPlaceholderCredentials(docuSignConfig)) {
            logger.warning("⚠️ DEMO MODE ACTIVATED - Using MockDocuSignService");
            return new MockDocuSignService(contractRepository);
        }

        logger.info("✅ PRODUCTION MODE - Using Real DocuSignServiceImpl");

        return new DocuSignServiceImpl(
                docuSignConfig,
                restTemplate,
                contractRepository,
                objectMapper
        );
    }

    private boolean isPlaceholderCredentials(DocuSignConfig config) {
        return config.getAccountId().contains("YOUR_") ||
                config.getClientId().contains("YOUR_") ||
                config.getClientSecret().contains("YOUR_") ||
                config.getImpersonatedUserId().contains("YOUR_");
    }
}