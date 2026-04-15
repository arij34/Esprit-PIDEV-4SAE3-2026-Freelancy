package tn.esprit.matching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling // ✅ ajouter
public class MatchingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchingApplication.class, args);
    }

}
