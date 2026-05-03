package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String questionText;
    private String questionType;
    private Double points;
    private String difficultyLevel;
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    @JsonBackReference("exam-question")
    private Exam exam;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JsonManagedReference("question-answer")
    private List<Answer> answers;
}
