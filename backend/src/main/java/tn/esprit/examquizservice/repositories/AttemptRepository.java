package tn.esprit.examquizservice.repositories;

import tn.esprit.examquizservice.entities.Attempt;
import tn.esprit.examquizservice.entities.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {
    
    // Find active attempt for a user on a specific exam
    Optional<Attempt> findByUserIdAndExamIdAndStatus(Long userId, Long examId, AttemptStatus status);
    
    // Find all attempts by user
    List<Attempt> findByUserId(Long userId);
    
    // Find all in-progress attempts
    List<Attempt> findByStatus(AttemptStatus status);

    // Find all in-progress attempts for an exam
    List<Attempt> findByExam_IdAndStatus(Long examId, AttemptStatus status);

    // Find attempts for an exam matching any of the given statuses — JOIN FETCH exam to avoid LazyInitializationException
    @Query("SELECT a FROM Attempt a JOIN FETCH a.exam WHERE a.exam.id = :examId AND a.status IN :statuses ORDER BY a.startTime DESC")
    List<Attempt> findByExamIdAndStatusIn(@Param("examId") Long examId, @Param("statuses") List<AttemptStatus> statuses);
    
    // Find attempt by session token
    Optional<Attempt> findBySessionToken(String sessionToken);
    
    // Check if user has active exam
    @Query("SELECT COUNT(a) > 0 FROM Attempt a WHERE a.userId = :userId AND a.status = 'IN_PROGRESS'")
    boolean hasActiveExam(@Param("userId") Long userId);
    
    // Find attempts with suspicious activity
    @Query("SELECT a FROM Attempt a WHERE a.suspiciousScore > :threshold")
    List<Attempt> findSuspiciousAttempts(@Param("threshold") Double threshold);
    
    // Find attempts with IP changes
    @Query("SELECT a FROM Attempt a WHERE a.ipAddress != :ipAddress AND a.userId = :userId AND a.status != 'SUBMITTED'")
    List<Attempt> findAttemptsWithIpChange(@Param("userId") Long userId, @Param("ipAddress") String ipAddress);
    
    // Find recent attempts for rate limiting
    @Query("SELECT COUNT(a) FROM Attempt a WHERE a.userId = :userId AND a.exam.id = :examId AND a.startTime > :startTime")
    int countRecentAttempts(@Param("userId") Long userId, @Param("examId") Long examId, @Param("startTime") LocalDateTime startTime);
    
    // Count answered questions
    @Query("SELECT COUNT(a) FROM Attempt att JOIN att.attemptAnswers a WHERE att.id = :attemptId AND a.selectedAnswer IS NOT NULL")
    int countAnsweredQuestions(@Param("attemptId") Long attemptId);
}
