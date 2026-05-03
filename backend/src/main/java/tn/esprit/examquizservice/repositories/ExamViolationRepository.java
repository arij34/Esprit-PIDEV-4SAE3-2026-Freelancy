package tn.esprit.examquizservice.repositories;

import tn.esprit.examquizservice.entities.ExamViolation;
import tn.esprit.examquizservice.entities.ExamViolationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface ExamViolationRepository extends JpaRepository<ExamViolation, Long> {
    
    Long countByUserIdAndExamId(Long userId, Long examId);
    
    List<ExamViolation> findByExamIdAndUserId(Long examId, Long userId);
    
    List<ExamViolation> findByExamId(Long examId);
    
    List<ExamViolation> findByUserId(Long userId);

    List<ExamViolation> findByAttemptId(Long attemptId);

    List<ExamViolation> findByExamIdAndAttemptIdIn(Long examId, List<Long> attemptIds);

    List<ExamViolation> findByExamIdAndTimestampAfterOrderByTimestampDesc(Long examId, LocalDateTime since);

    Long countByExamIdAndUserIdAndType(Long examId, Long userId, ExamViolationType type);
}
