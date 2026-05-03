package tn.esprit.examquizservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exam_results",
        indexes = {
                @Index(name = "idx_result_exam_user", columnList = "exam_id, user_id"),
                @Index(name = "idx_result_attempt",  columnList = "attempt_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long examId;

    private Long userId;

    private Long attemptId;

    private Double scorePercent;

    private Double earnedPoints;

    private Double totalPoints;

    @Enumerated(EnumType.STRING)
    private ResultStatus status;

    private LocalDateTime submittedAt;

    private Integer timeTakenSeconds;

    @Column(columnDefinition = "TEXT")
    private String answersJson;
}
