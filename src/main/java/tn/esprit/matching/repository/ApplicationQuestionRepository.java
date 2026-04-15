package tn.esprit.matching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.matching.entity.ApplicationQuestion;

import java.util.List;

public interface ApplicationQuestionRepository extends JpaRepository<ApplicationQuestion, Long> {
    List<ApplicationQuestion> findAllByOrderByOrderIndexAsc();
}