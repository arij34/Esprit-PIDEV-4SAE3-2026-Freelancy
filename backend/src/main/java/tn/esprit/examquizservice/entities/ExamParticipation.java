package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "exam_participations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    @JsonIgnoreProperties({"questions", "examSetting", "quizSetting", "attempts"})
    private Exam exam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamParticipationStatus status;

    private LocalDateTime joinedAt;
    private LocalDateTime completedAt;
}
