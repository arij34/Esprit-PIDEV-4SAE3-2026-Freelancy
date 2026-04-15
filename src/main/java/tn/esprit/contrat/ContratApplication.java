package tn.esprit.contrat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients(basePackages = "tn.esprit.contrat.clients")
@EnableAsync  // Nécessaire pour que les emails soient envoyés en arrière-plan (@Async)
public class ContratApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContratApplication.class, args);
    }
}