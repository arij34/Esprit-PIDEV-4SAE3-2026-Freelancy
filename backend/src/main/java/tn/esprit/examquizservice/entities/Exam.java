package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private Integer duration;
    private Double points;
    private Double passingScore;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxAttempts;
    private String createdBy;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private ExamStatus status;
    @Enumerated(EnumType.STRING)
    private ExamType examType;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JsonManagedReference("exam-question")
    private List<Question> questions;

    @OneToOne(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JsonManagedReference("exam-setting")
    private ExamSetting examSetting;

    @OneToOne(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JsonManagedReference("exam-quiz-setting")
    private QuizSetting quizSetting;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @JsonManagedReference("exam-attempt")
    private List<Attempt> attempts;
}
