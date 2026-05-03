package tn.esprit.examquizservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exam_violations",
        indexes = {
                @Index(name = "idx_violation_exam_user", columnList = "exam_id, user_id"),
                @Index(name = "idx_violation_attempt",  columnList = "attempt_id"),
                @Index(name = "idx_violation_timestamp", columnList = "timestamp")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long examId;
    private Long userId;
    private Long attemptId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamViolationType type;

    @Enumerated(EnumType.STRING)
    private ViolationSeverity severity;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * Action taken in response to this violation (WARNING, AUTO_SUBMIT, TERMINATE, NONE)
     */
    private String actionTaken;

    /**
     * Snapshot of the total violation count at the time this event was recorded
     */
    private Integer countSnapshot;

    @Version
    private Long version;

}
