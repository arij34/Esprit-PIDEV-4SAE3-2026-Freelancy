package tn.esprit.examquizservice.repositories;

import tn.esprit.examquizservice.entities.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
	List<Answer> findByQuestionId(Long questionId);
}
