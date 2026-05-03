package tn.esprit.examquizservice.repositories;

import tn.esprit.examquizservice.entities.CheatingLog;
import tn.esprit.examquizservice.entities.CheatingEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface CheatingLogRepository extends JpaRepository<CheatingLog, Long> {
    
    // Find all cheating logs for an attempt
    List<CheatingLog> findByAttemptId(Long attemptId);

    // JOIN FETCH attempt so cheatingLog.getAttempt() is never lazy when used outside a transaction
    @Query("SELECT c FROM CheatingLog c JOIN FETCH c.attempt a WHERE a.id IN :attemptIds")
    List<CheatingLog> findByAttemptIdIn(@Param("attemptIds") List<Long> attemptIds);

    @Query("SELECT c FROM CheatingLog c JOIN FETCH c.attempt a JOIN FETCH a.exam WHERE a.exam.id = :examId AND c.eventTime >= :since ORDER BY c.eventTime DESC")
    List<CheatingLog> findRecentByExamId(@Param("examId") Long examId, @Param("since") LocalDateTime since);
    
    // Count specific event types for an attempt
    @Query("SELECT COUNT(c) FROM CheatingLog c WHERE c.attempt.id = :attemptId AND c.eventType = :eventType")
    int countEventType(@Param("attemptId") Long attemptId, @Param("eventType") CheatingEventType eventType);
    
    // Count all events for an attempt
    @Query("SELECT COUNT(c) FROM CheatingLog c WHERE c.attempt.id = :attemptId")
    int countByAttemptId(@Param("attemptId") Long attemptId);
    
    // Find attempts with high cheating activity
    @Query("SELECT DISTINCT c.attempt FROM CheatingLog c GROUP BY c.attempt HAVING COUNT(c) > :threshold")
    List<CheatingLog> findHighCheatingActivity(@Param("threshold") int threshold);
}
