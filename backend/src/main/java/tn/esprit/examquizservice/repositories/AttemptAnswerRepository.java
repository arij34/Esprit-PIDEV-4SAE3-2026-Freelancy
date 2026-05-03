package tn.esprit.examquizservice.repositories;

import tn.esprit.examquizservice.entities.AttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {
}
