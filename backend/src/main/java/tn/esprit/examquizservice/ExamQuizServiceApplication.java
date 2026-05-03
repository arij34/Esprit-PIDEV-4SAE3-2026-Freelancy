package tn.esprit.examquizservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ExamQuizServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamQuizServiceApplication.class, args);
    }

}
