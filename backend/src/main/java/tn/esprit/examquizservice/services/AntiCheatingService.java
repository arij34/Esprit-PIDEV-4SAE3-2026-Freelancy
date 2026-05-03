package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.*;
import tn.esprit.examquizservice.repositories.AttemptRepository;
import tn.esprit.examquizservice.repositories.CheatingLogRepository;
import tn.esprit.examquizservice.dtos.CheatingEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AntiCheatingService {
    
    private final AttemptRepository attemptRepository;
    private final CheatingLogRepository cheatingLogRepository;
    
    @Value("${exam.cheating.threshold:5.0}")
    private Double suspiciousScoreThreshold;
    
    @Value("${exam.cheating.tab-switch-weight:1.0}")
    private Double tabSwitchWeight;
    
    @Value("${exam.cheating.fullscreen-exit-weight:2.0}")
    private Double fullscreenExitWeight;
    
    @Value("${exam.cheating.copy-paste-weight:3.0}")
    private Double copyPasteWeight;
    
    @Value("${exam.cheating.ip-change-weight:3.0}")
    private Double ipChangeWeight;
    
    @Value("${exam.cheating.device-change-weight:3.0}")
    private Double deviceChangeWeight;
    
    @Value("${exam.cheating.fast-answer-weight:0.5}")
    private Double fastAnswerWeight;
    
    /**
     * Validate exam session token and IP/Device
     */
    public boolean validateSessionToken(String sessionToken, String ipAddress, String deviceFingerprint) {
        Optional<Attempt> attempt = attemptRepository.findBySessionToken(sessionToken);
        
        if (attempt.isEmpty()) {
            log.warn("Invalid session token: {}", sessionToken);
            return false;
        }
        
        Attempt attData = attempt.get();
        
        // Check if session is expired
        if (attData.getExpectedEndTime() != null && attData.getExpectedEndTime() < System.currentTimeMillis()) {
            log.warn("Session token expired for attempt: {}", attData.getId());
            return false;
        }
        
        return true;
    }
    
    /**
     * Record a cheating event and update suspicious score
     */
    public void recordCheatingEvent(Long attemptId, CheatingEventDTO eventDTO) {
        Optional<Attempt> attempt = attemptRepository.findById(attemptId);
        
        if (attempt.isEmpty()) {
            log.error("Attempt not found: {}", attemptId);
            return;
        }
        
        Attempt att = attempt.get();
        
        // Create and save cheating log
        CheatingLog log = CheatingLog.builder()
            .attempt(att)
            .eventType(eventDTO.getEventType())
            .eventTime(LocalDateTime.now())
            .details(eventDTO.getDetails())
            .build();
        
        cheatingLogRepository.save(log);
        
        // Update suspicious score based on event type
        updateSuspiciousScore(att, eventDTO.getEventType());
        
        // Log suspicious activity
        if (att.getSuspiciousActivities() == null) {
            att.setSuspiciousActivities("");
        }
        att.setSuspiciousActivities(
            att.getSuspiciousActivities() + 
            eventDTO.getEventType() + " at " + LocalDateTime.now() + "; "
        );
        
        attemptRepository.save(att);
    }
    
    /**
     * Update suspicious score based on event type
     */
    private void updateSuspiciousScore(Attempt attempt, CheatingEventType eventType) {
        Double currentScore = attempt.getSuspiciousScore() != null ? attempt.getSuspiciousScore() : 0.0;
        Double newScore = currentScore;
        
        switch (eventType) {
            case TAB_SWITCH:
                attempt.setTabSwitchCount((attempt.getTabSwitchCount() != null ? attempt.getTabSwitchCount() : 0) + 1);
                newScore += tabSwitchWeight;
                break;
            case FULLSCREEN_EXIT:
                attempt.setFullscreenExitCount((attempt.getFullscreenExitCount() != null ? attempt.getFullscreenExitCount() : 0) + 1);
                newScore += fullscreenExitWeight;
                break;
            case COPY_PASTE:
                attempt.setCopyPasteDetected(true);
                newScore += copyPasteWeight;
                break;
            case IP_CHANGED:
                newScore += ipChangeWeight;
                break;
            case DEVICE_CHANGED:
                newScore += deviceChangeWeight;
                break;
            case PAGE_REFRESH:
                newScore += 1.5;
                break;
            case KEYBOARD_SHORTCUT:
                newScore += 1.0;
                break;
            case RIGHT_CLICK:
                newScore += 0.5;
                break;
            default:
                newScore += 1.0;
        }
        
        attempt.setSuspiciousScore(newScore);
    }
    
    /**
     * Check if IP address changed from start of exam
     */
    public boolean checkIpAddressChange(Attempt attempt, String currentIpAddress) {
        if (attempt.getIpAddress() == null || !attempt.getIpAddress().equals(currentIpAddress)) {
            if (attempt.getLastIpAddress() != null && !attempt.getLastIpAddress().equals(currentIpAddress)) {
                logIPChange(attempt, currentIpAddress);
                return true;
            }
            attempt.setLastIpAddress(currentIpAddress);
            return false;
        }
        return false;
    }
    
    /**
     * Check if device fingerprint changed from start of exam
     */
    public boolean checkDeviceFingerprint(Attempt attempt, String currentFingerprint) {
        if (attempt.getDeviceFingerprint() == null || !attempt.getDeviceFingerprint().equals(currentFingerprint)) {
            if (attempt.getLastDeviceFingerprint() != null && !attempt.getLastDeviceFingerprint().equals(currentFingerprint)) {
                logDeviceChange(attempt, currentFingerprint);
                return true;
            }
            attempt.setLastDeviceFingerprint(currentFingerprint);
            return false;
        }
        return false;
    }
    
    /**
     * Check if answer was answered too quickly (potential bot/copy)
     */
    public boolean checkTooFastAnswer(Integer timeTakenSeconds) {
        // Consider anything under 3 seconds as suspiciously fast
        return timeTakenSeconds != null && timeTakenSeconds < 3;
    }
    
    /**
     * Calculate final suspicious score and determine if attempt should be flagged
     */
    public void calculateFinalSuspiciousScore(Attempt attempt) {
        List<CheatingLog> logs = cheatingLogRepository.findByAttemptId(attempt.getId());
        Double score = 0.0;
        
        for (CheatingLog log : logs) {
            switch (log.getEventType()) {
                case TAB_SWITCH:
                    score += tabSwitchWeight;
                    break;
                case FULLSCREEN_EXIT:
                    score += fullscreenExitWeight;
                    break;
                case COPY_PASTE:
                    score += copyPasteWeight;
                    break;
                case IP_CHANGED:
                    score += ipChangeWeight;
                    break;
                case DEVICE_CHANGED:
                    score += deviceChangeWeight;
                    break;
                default:
                    score += 1.0;
            }
        }
        
        attempt.setSuspiciousScore(score);
        
        // Flag attempt if score exceeds threshold
        if (score > suspiciousScoreThreshold) {
            attempt.setStatus(AttemptStatus.AUTO_SUBMITTED);
        }
        
        attemptRepository.save(attempt);
    }
    
    /**
     * Check if user already has an active exam session (prevent multiple sessions)
     */
    public boolean hasActiveExamSession(Long userId) {
        return attemptRepository.hasActiveExam(userId);
    }
    
    /**
     * Get suspicious attempts for admin review
     */
    public List<Attempt> getSuspiciousAttempts() {
        return attemptRepository.findSuspiciousAttempts(suspiciousScoreThreshold);
    }
    
    private void logIPChange(Attempt attempt, String newIpAddress) {
        CheatingLog log = CheatingLog.builder()
            .attempt(attempt)
            .eventType(CheatingEventType.IP_CHANGED)
            .eventTime(LocalDateTime.now())
            .details("IP changed from " + attempt.getIpAddress() + " to " + newIpAddress)
            .build();
        cheatingLogRepository.save(log);
    }
    
    private void logDeviceChange(Attempt attempt, String newFingerprint) {
        CheatingLog log = CheatingLog.builder()
            .attempt(attempt)
            .eventType(CheatingEventType.DEVICE_CHANGED)
            .eventTime(LocalDateTime.now())
            .details("Device fingerprint changed from " + attempt.getDeviceFingerprint() + " to " + newFingerprint)
            .build();
        cheatingLogRepository.save(log);
    }
}
