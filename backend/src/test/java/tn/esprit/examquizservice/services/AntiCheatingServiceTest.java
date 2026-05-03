package tn.esprit.examquizservice.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tn.esprit.examquizservice.dtos.CheatingEventDTO;
import tn.esprit.examquizservice.entities.Attempt;
import tn.esprit.examquizservice.entities.AttemptStatus;
import tn.esprit.examquizservice.entities.CheatingEventType;
import tn.esprit.examquizservice.entities.CheatingLog;
import tn.esprit.examquizservice.repositories.AttemptRepository;
import tn.esprit.examquizservice.repositories.CheatingLogRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AntiCheatingServiceTest {

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private CheatingLogRepository cheatingLogRepository;

    @InjectMocks
    private AntiCheatingService antiCheatingService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(antiCheatingService, "suspiciousScoreThreshold", 5.0);
        ReflectionTestUtils.setField(antiCheatingService, "tabSwitchWeight", 1.0);
        ReflectionTestUtils.setField(antiCheatingService, "fullscreenExitWeight", 2.0);
        ReflectionTestUtils.setField(antiCheatingService, "copyPasteWeight", 3.0);
        ReflectionTestUtils.setField(antiCheatingService, "ipChangeWeight", 3.0);
        ReflectionTestUtils.setField(antiCheatingService, "deviceChangeWeight", 3.0);
    }

    @Test
    void validateSessionTokenShouldReturnFalseWhenMissing() {
        when(attemptRepository.findBySessionToken("missing")).thenReturn(Optional.empty());

        assertFalse(antiCheatingService.validateSessionToken("missing", "1.1.1.1", "fp"));
    }

    @Test
    void validateSessionTokenShouldReturnFalseWhenExpired() {
        Attempt attempt = new Attempt();
        attempt.setId(1L);
        attempt.setExpectedEndTime(System.currentTimeMillis() - 1000);

        when(attemptRepository.findBySessionToken("expired")).thenReturn(Optional.of(attempt));

        assertFalse(antiCheatingService.validateSessionToken("expired", "1.1.1.1", "fp"));
    }

    @Test
    void validateSessionTokenShouldReturnTrueWhenValid() {
        Attempt attempt = new Attempt();
        attempt.setExpectedEndTime(System.currentTimeMillis() + 10000);

        when(attemptRepository.findBySessionToken("valid")).thenReturn(Optional.of(attempt));

        assertTrue(antiCheatingService.validateSessionToken("valid", "1.1.1.1", "fp"));
    }

    @Test
    void recordCheatingEventShouldReturnWhenAttemptNotFound() {
        when(attemptRepository.findById(1L)).thenReturn(Optional.empty());

        antiCheatingService.recordCheatingEvent(1L, CheatingEventDTO.builder().eventType(CheatingEventType.TAB_SWITCH).build());

        verify(cheatingLogRepository, never()).save(any(CheatingLog.class));
    }

    @Test
    void recordCheatingEventShouldUpdateScoreAndCounters() {
        Attempt attempt = new Attempt();
        attempt.setId(2L);
        attempt.setSuspiciousScore(0.0);

        when(attemptRepository.findById(2L)).thenReturn(Optional.of(attempt));

        CheatingEventDTO event = CheatingEventDTO.builder()
                .eventType(CheatingEventType.TAB_SWITCH)
                .details("switch")
                .build();

        antiCheatingService.recordCheatingEvent(2L, event);

        assertEquals(1, attempt.getTabSwitchCount());
        assertEquals(1.0, attempt.getSuspiciousScore());
        assertTrue(attempt.getSuspiciousActivities().contains("TAB_SWITCH"));
        verify(cheatingLogRepository).save(any(CheatingLog.class));
        verify(attemptRepository).save(attempt);
    }

    @Test
    void checkIpAddressChangeShouldDetectSecondChange() {
        Attempt attempt = new Attempt();
        attempt.setIpAddress("1.1.1.1");

        boolean first = antiCheatingService.checkIpAddressChange(attempt, "2.2.2.2");
        boolean second = antiCheatingService.checkIpAddressChange(attempt, "3.3.3.3");

        assertFalse(first);
        assertTrue(second);
        verify(cheatingLogRepository).save(any(CheatingLog.class));
    }

    @Test
    void checkDeviceFingerprintShouldDetectSecondChange() {
        Attempt attempt = new Attempt();
        attempt.setDeviceFingerprint("fp1");

        boolean first = antiCheatingService.checkDeviceFingerprint(attempt, "fp2");
        boolean second = antiCheatingService.checkDeviceFingerprint(attempt, "fp3");

        assertFalse(first);
        assertTrue(second);
        verify(cheatingLogRepository).save(any(CheatingLog.class));
    }

    @Test
    void checkTooFastAnswerShouldMatchThreshold() {
        assertTrue(antiCheatingService.checkTooFastAnswer(2));
        assertFalse(antiCheatingService.checkTooFastAnswer(3));
        assertFalse(antiCheatingService.checkTooFastAnswer(null));
    }

    @Test
    void calculateFinalSuspiciousScoreShouldAutoSubmitWhenAboveThreshold() {
        Attempt attempt = new Attempt();
        attempt.setId(10L);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);

        List<CheatingLog> logs = List.of(
                CheatingLog.builder().eventType(CheatingEventType.FULLSCREEN_EXIT).build(),
                CheatingLog.builder().eventType(CheatingEventType.COPY_PASTE).build(),
                CheatingLog.builder().eventType(CheatingEventType.TAB_SWITCH).build()
        );

        when(cheatingLogRepository.findByAttemptId(10L)).thenReturn(logs);

        antiCheatingService.calculateFinalSuspiciousScore(attempt);

        assertEquals(6.0, attempt.getSuspiciousScore());
        assertEquals(AttemptStatus.AUTO_SUBMITTED, attempt.getStatus());
        verify(attemptRepository).save(attempt);
    }

    @Test
    void hasActiveSessionAndSuspiciousAttemptsShouldDelegate() {
        when(attemptRepository.hasActiveExam(5L)).thenReturn(true);
        when(attemptRepository.findSuspiciousAttempts(5.0)).thenReturn(List.of(new Attempt()));

        assertTrue(antiCheatingService.hasActiveExamSession(5L));
        assertEquals(1, antiCheatingService.getSuspiciousAttempts().size());
    }
}
