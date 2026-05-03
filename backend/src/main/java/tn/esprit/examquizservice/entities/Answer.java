package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String answerText;
    private Boolean isCorrect;
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonBackReference("question-answer")
    private Question question;
}
