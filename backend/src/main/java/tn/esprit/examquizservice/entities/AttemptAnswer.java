package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attempt_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String textAnswer;
    private Boolean isCorrect;
    private Double pointsEarned;
    private LocalDateTime answeredAt;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    @JsonBackReference("attempt-answer")
    private Attempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "selected_answer_id")
    private Answer selectedAnswer;
}
