package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean requireFullscreen;
    private Boolean preventTabSwitch;
    private Boolean preventCopyPaste;
    private Boolean randomizeQuestions;
    private Boolean randomizeAnswers;
    private Boolean showTimer;
    private Boolean autoSubmitOnTabSwitchLimit;
    private Integer tabSwitchLimit;
    private Boolean webcamRequired;
    private Boolean browserLock;
    private Boolean ipRestriction;
    private Boolean oneAttemptPerUser;
    private Boolean oneQuestionPerPage;
    private Boolean showResult;
    
    // Anti-Cheating Settings
    private Boolean deviceFingerprintRequired;
    private Boolean enableSecureSessionToken;
    private Boolean enableDeviceFingerprinting;
    private Double suspiciousScoreThreshold;
    private Boolean autoSubmitOnHighScore;
    private String strictnessLevel; // LOW, MEDIUM, HIGH
    private Boolean detectScreenRecording;
    private Boolean detectVpnProxy;
    private Integer minutesBetweenAttempts;

    @OneToOne
    @JoinColumn(name = "exam_id")
    @JsonBackReference("exam-setting")
    private Exam exam;
}
