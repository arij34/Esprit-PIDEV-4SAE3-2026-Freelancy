package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean randomizeQuestionOrder;
    private Boolean randomizeAnswerOrder;
    private Boolean timeLimitPerQuestion;
    private Boolean practiceMode;

    @OneToOne
    @JoinColumn(name = "exam_id")
    @JsonBackReference("exam-quiz-setting")
    private Exam exam;
}
