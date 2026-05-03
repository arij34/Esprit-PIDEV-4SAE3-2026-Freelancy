package tn.esprit.examquizservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.examquizservice.entities.ExamResult;

import java.util.List;
import java.util.Optional;

public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    Optional<ExamResult> findByAttemptId(Long attemptId);

    Optional<ExamResult> findByUserIdAndExamId(Long userId, Long examId);

    List<ExamResult> findByUserId(Long userId);

    List<ExamResult> findAllByUserIdAndExamId(Long userId, Long examId);

    Optional<ExamResult> findTopByUserIdOrderBySubmittedAtDescIdDesc(Long userId);
}
