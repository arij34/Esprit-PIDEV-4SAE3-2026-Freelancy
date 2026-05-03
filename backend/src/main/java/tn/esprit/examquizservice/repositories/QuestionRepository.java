package tn.esprit.examquizservice.repositories;

import tn.esprit.examquizservice.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
	@Query("select q from Question q where q.exam.id = :examId")
	List<Question> findByExamId(@Param("examId") Long examId);
}
