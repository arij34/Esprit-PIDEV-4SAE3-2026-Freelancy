package tn.esprit.examquizservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tn.esprit.examquizservice.dtos.RecordViolationResponse;
import tn.esprit.examquizservice.dtos.ViolationDTO;
import tn.esprit.examquizservice.dtos.ViolationStatus;
import tn.esprit.examquizservice.entities.ExamViolation;
import tn.esprit.examquizservice.entities.ExamViolationType;
import tn.esprit.examquizservice.entities.ViolationSeverity;
import tn.esprit.examquizservice.repositories.ExamViolationRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProctoringServiceTest {

    @Mock
    private ExamViolationRepository violationRepository;
    @Mock
    private ExamSessionService examSessionService;
    @Mock
    private AttemptService attemptService;

    @InjectMocks
    private ProctoringService service;

    @BeforeEach
    void setUpThresholds() {
        ReflectionTestUtils.setField(service, "defaultThreshold", 3);
        ReflectionTestUtils.setField(service, "phoneDetectedThreshold", 2);
        ReflectionTestUtils.setField(service, "tabSwitchThreshold", 3);
        ReflectionTestUtils.setField(service, "lookingAwayThreshold", 4);
    }

    @Test
    void recordViolationShouldReturnWarningBeforeThreshold() {
        when(violationRepository.countByUserIdAndExamId(11L, 22L)).thenReturn(0L);
        when(violationRepository.countByExamIdAndUserIdAndType(22L, 11L, ExamViolationType.TAB_SWITCH))
                .thenReturn(1L);
        when(violationRepository.save(any(ExamViolation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecordViolationResponse response = service.recordViolation(
                22L, 11L, 33L, ExamViolationType.TAB_SWITCH, "medium", "tab changed");

        assertEquals(ViolationStatus.WARNING, response.getStatus());
        assertEquals("CONTINUE", response.getAction());
        assertTrue(response.getMessage().contains("remaining"));

        ArgumentCaptor<ExamViolation> captor = ArgumentCaptor.forClass(ExamViolation.class);
        verify(violationRepository).save(captor.capture());
        ExamViolation saved = captor.getValue();
        assertEquals("WARNING", saved.getActionTaken());
        assertEquals(ViolationSeverity.MEDIUM, saved.getSeverity());
        assertEquals(1, saved.getCountSnapshot());
    }

    @Test
    void recordViolationShouldAutoSubmitWhenTypeThresholdReached() {
        when(violationRepository.countByUserIdAndExamId(11L, 22L)).thenReturn(2L);
        when(violationRepository.countByExamIdAndUserIdAndType(22L, 11L, ExamViolationType.PHONE_DETECTED))
                .thenReturn(1L);
        when(violationRepository.save(any(ExamViolation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecordViolationResponse response = service.recordViolation(
                22L, 11L, 33L, ExamViolationType.PHONE_DETECTED, "critical", "phone in frame");

        assertEquals(ViolationStatus.AUTO_SUBMITTED, response.getStatus());
        assertEquals("AUTO_SUBMIT", response.getAction());
        verify(examSessionService).autoSubmitExam(33L);
    }

    @Test
    void getViolationsAndSummaryShouldMapRepositoryData() {
        ExamViolation v1 = ExamViolation.builder()
                .id(1L)
                .examId(22L)
                .userId(11L)
                .attemptId(33L)
                .type(ExamViolationType.TAB_SWITCH)
                .severity(ViolationSeverity.HIGH)
                .timestamp(LocalDateTime.now().minusMinutes(5))
                .details("d1")
                .actionTaken("WARNING")
                .countSnapshot(1)
                .build();

        ExamViolation v2 = ExamViolation.builder()
                .id(2L)
                .examId(22L)
                .userId(11L)
                .attemptId(33L)
                .type(ExamViolationType.PHONE_DETECTED)
                .severity(ViolationSeverity.CRITICAL)
                .timestamp(LocalDateTime.now())
                .details("d2")
                .actionTaken("AUTO_SUBMIT")
                .countSnapshot(2)
                .build();

        when(violationRepository.findByExamIdAndUserId(22L, 11L)).thenReturn(List.of(v1, v2));

        List<ViolationDTO> dtos = service.getViolationsByExamAndUser(22L, 11L);
        assertEquals(2, dtos.size());
        assertEquals(ExamViolationType.PHONE_DETECTED, dtos.get(1).getType());

        var summary = service.getViolationSummary(22L, 11L);
        assertEquals(22L, summary.get("examId"));
        assertEquals(11L, summary.get("userId"));
        assertEquals(2L, summary.get("totalViolations"));
        assertEquals("AUTO_SUBMIT", summary.get("lastAction"));
        assertNotNull(summary.get("totalViolations"));
    }
}
