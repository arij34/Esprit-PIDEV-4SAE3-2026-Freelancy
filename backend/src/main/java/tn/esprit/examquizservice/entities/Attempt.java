package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double score;
    @Enumerated(EnumType.STRING)
    private AttemptStatus status;
    private Integer tabSwitchCount;
    private Integer fullscreenExitCount;
    private Boolean copyPasteDetected;
    private Double suspiciousScore;
    private String ipAddress;
    private String lastIpAddress;
    private String deviceFingerprint;
    private String lastDeviceFingerprint;
    private String sessionToken;
    private Integer answerChangeCount;
    private Integer tooFastAnswerCount;
    private Long expectedEndTime;
    private Boolean autoSubmitted;
    private LocalDateTime submittedAt;
    @Column(columnDefinition = "TEXT")
    private String cameraSnapshots;
    private Boolean faceDetected;
    private Boolean multipleFacesDetected;
    private String browserInfo;
    @Column(columnDefinition = "TEXT")
    private String suspiciousActivities;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    @JsonBackReference("exam-attempt")
    private Exam exam;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("attempt-answer")
    private List<AttemptAnswer> attemptAnswers;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("attempt-cheating")
    private List<CheatingLog> cheatingLogs;
}
