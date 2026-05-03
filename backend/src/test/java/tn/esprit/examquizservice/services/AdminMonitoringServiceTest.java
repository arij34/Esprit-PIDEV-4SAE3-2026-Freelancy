package tn.esprit.examquizservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.examquizservice.dtos.AdminLiveEventDTO;
import tn.esprit.examquizservice.dtos.AdminLiveExamSnapshotDTO;
import tn.esprit.examquizservice.entities.Attempt;
import tn.esprit.examquizservice.entities.AttemptStatus;
import tn.esprit.examquizservice.entities.CheatingEventType;
import tn.esprit.examquizservice.entities.CheatingLog;
import tn.esprit.examquizservice.entities.Exam;
import tn.esprit.examquizservice.entities.ExamParticipation;
import tn.esprit.examquizservice.entities.ExamViolation;
import tn.esprit.examquizservice.entities.ExamViolationType;
import tn.esprit.examquizservice.entities.ViolationSeverity;
import tn.esprit.examquizservice.repositories.AttemptRepository;
import tn.esprit.examquizservice.repositories.CheatingLogRepository;
import tn.esprit.examquizservice.repositories.ExamParticipationRepository;
import tn.esprit.examquizservice.repositories.ExamRepository;
import tn.esprit.examquizservice.repositories.ExamViolationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMonitoringServiceTest {

    @Mock
    private AttemptRepository attemptRepository;
    @Mock
    private ExamViolationRepository examViolationRepository;
    @Mock
    private CheatingLogRepository cheatingLogRepository;
    @Mock
    private ExamRepository examRepository;
    @Mock
    private ExamParticipationRepository participationRepository;

    @InjectMocks
    private AdminMonitoringService service;

    @Test
    void getLiveSnapshotShouldIncludeMappedCandidatesAndCounts() {
        Exam exam = new Exam();
        exam.setId(100L);
        exam.setTitle("Monitoring Exam");

        Attempt inProgress = Attempt.builder()
                .id(1L)
                .exam(exam)
                .userId(7L)
                .status(AttemptStatus.IN_PROGRESS)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .suspiciousScore(1.5)
                .autoSubmitted(false)
                .build();

        Attempt submitted = Attempt.builder()
                .id(2L)
                .exam(exam)
                .userId(8L)
                .status(AttemptStatus.SUBMITTED)
                .startTime(LocalDateTime.now().minusMinutes(20))
                .submittedAt(LocalDateTime.now().minusMinutes(1))
                .suspiciousScore(6.0)
                .autoSubmitted(true)
                .build();

        ExamParticipation p1 = ExamParticipation.builder().exam(exam).userId(7L)
                .userFirstName("Alice").userLastName("A").userEmail("a@test.com").build();
        ExamParticipation p2 = ExamParticipation.builder().exam(exam).userId(8L)
                .userFirstName("Bob").userLastName("B").userEmail("b@test.com").build();

        ExamViolation v = ExamViolation.builder()
                .id(99L)
                .examId(100L)
                .attemptId(2L)
                .userId(8L)
                .type(ExamViolationType.PHONE_DETECTED)
                .severity(ViolationSeverity.HIGH)
                .actionTaken("AUTO_SUBMIT")
                .timestamp(LocalDateTime.now().minusMinutes(2))
                .build();

        CheatingLog log = CheatingLog.builder()
                .id(55L)
                .attempt(submitted)
                .eventType(CheatingEventType.TAB_SWITCH)
                .details("tab changed")
                .eventTime(LocalDateTime.now().minusMinutes(3))
                .build();

        when(examRepository.findById(100L)).thenReturn(Optional.of(exam));
        when(attemptRepository.findByExamIdAndStatusIn(eq(100L), any()))
                .thenReturn(List.of(inProgress, submitted));
        when(participationRepository.findByExam_Id(100L)).thenReturn(List.of(p1, p2));
        when(examViolationRepository.findByExamIdAndAttemptIdIn(100L, List.of(1L, 2L))).thenReturn(List.of(v));
        when(cheatingLogRepository.findByAttemptIdIn(List.of(1L, 2L))).thenReturn(List.of(log));
        when(attemptRepository.countAnsweredQuestions(1L)).thenReturn(3);
        when(attemptRepository.countAnsweredQuestions(2L)).thenReturn(5);
        when(examViolationRepository.findByExamIdAndTimestampAfterOrderByTimestampDesc(eq(100L), any()))
                .thenReturn(List.of(v));
        when(cheatingLogRepository.findRecentByExamId(eq(100L), any())).thenReturn(List.of(log));

        AdminLiveExamSnapshotDTO snapshot = service.getLiveSnapshot(100L);

        assertEquals(100L, snapshot.getExamId());
        assertEquals("Monitoring Exam", snapshot.getExamTitle());
        assertEquals(1, snapshot.getActiveCandidates());
        assertEquals(2, snapshot.getTotalParticipants());
        assertEquals(2, snapshot.getCandidates().size());
                assertTrue(snapshot.getCandidates().stream().anyMatch(c -> "HIGH".equals(c.getRiskLevel())));
        assertNotNull(snapshot.getGeneratedAt());
    }

    @Test
    void getRecentEventsShouldMergeSortAndApplyLimit() {
        Exam exam = new Exam();
        exam.setId(100L);

        Attempt attempt = Attempt.builder().id(10L).exam(exam).userId(7L).build();

        ExamViolation olderViolation = ExamViolation.builder()
                .examId(100L)
                .attemptId(10L)
                .userId(7L)
                .type(ExamViolationType.TAB_SWITCH)
                .severity(ViolationSeverity.MEDIUM)
                .timestamp(LocalDateTime.now().minusMinutes(2))
                .build();

        CheatingLog newerLog = CheatingLog.builder()
                .attempt(attempt)
                .eventType(CheatingEventType.TAB_SWITCH)
                .details("phone")
                .eventTime(LocalDateTime.now().minusMinutes(1))
                .build();

        when(examViolationRepository.findByExamIdAndTimestampAfterOrderByTimestampDesc(eq(100L), any()))
                .thenReturn(List.of(olderViolation));
        when(cheatingLogRepository.findRecentByExamId(eq(100L), any())).thenReturn(List.of(newerLog));

        List<AdminLiveEventDTO> events = service.getRecentEvents(100L, 30, 1);

        assertEquals(1, events.size());
        assertEquals("CHEATING_LOG", events.get(0).getSource());
                assertEquals("TAB_SWITCH", events.get(0).getType());
    }

    @Test
    void getCandidateActivityShouldReturnActiveOnlyForInProgress() {
        Exam exam = new Exam();
        exam.setId(100L);
        exam.setTitle("Monitoring Exam");

        Attempt submitted = Attempt.builder()
                .id(2L)
                .exam(exam)
                .userId(8L)
                .status(AttemptStatus.SUBMITTED)
                .startTime(LocalDateTime.now().minusMinutes(20))
                .submittedAt(LocalDateTime.now().minusMinutes(5))
                .build();

        ExamParticipation participation = ExamParticipation.builder()
                .exam(exam)
                .userId(8L)
                .userFirstName("Bob")
                .userLastName("B")
                .userEmail("b@test.com")
                .build();

        when(examRepository.findById(100L)).thenReturn(Optional.of(exam));
        when(participationRepository.findByExam_IdAndUserId(100L, 8L)).thenReturn(Optional.of(participation));
        when(attemptRepository.findByExamIdAndStatusIn(eq(100L), any())).thenReturn(List.of(submitted));

        Map<String, Object> activity = service.getCandidateActivity(100L, 8L);

        assertEquals("Monitoring Exam", activity.get("examTitle"));
        assertEquals("Bob", activity.get("userFirstName"));
        assertEquals("SUBMITTED", activity.get("attemptStatus"));
        assertFalse((Boolean) activity.get("active"));
        assertTrue(activity.containsKey("generatedAt"));
    }
}
