package tn.esprit.examquizservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLiveCandidateDTO {

    private Long attemptId;
    private Long examId;
    private String examTitle;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String attemptStatus;
    private LocalDateTime startedAt;
    private Long expectedEndTime;
    private Double suspiciousScore;
    private Integer answeredQuestions;
    private Integer cheatingEventsCount;
    private Integer violationCount;
    private String lastWarningType;
    private String lastWarningAction;
    private LocalDateTime lastWarningTime;
    private LocalDateTime lastActivityTime;
    private Boolean autoSubmitted;
    /** LOW / MEDIUM / HIGH based on suspiciousScore + violationCount */
    private String riskLevel;
}