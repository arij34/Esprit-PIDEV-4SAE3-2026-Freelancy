package tn.esprit.contrat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Expose les PDF générés (dossier ./pdfs/) comme ressources statiques.
 * Exemple d'URL : http://localhost:8097/pdfs/contract-44.pdf
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mappe toute URL /pdfs/** vers le dossier local ./pdfs/
        registry.addResourceHandler("/pdfs/**")
                .addResourceLocations("file:./pdfs/");
    }
}
