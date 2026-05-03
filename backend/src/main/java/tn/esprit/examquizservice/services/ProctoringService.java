package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.config.ProctoringConfig;
import tn.esprit.examquizservice.dtos.RecordViolationResponse;
import tn.esprit.examquizservice.dtos.ViolationDTO;
import tn.esprit.examquizservice.dtos.ViolationStatus;
import tn.esprit.examquizservice.entities.*;
import tn.esprit.examquizservice.repositories.ExamViolationRepository;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling exam proctoring and violation tracking.
 * - Implements violation recording with business rules
 * - Manages automatic exam termination and submission
 * - Tracks violation history for reporting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProctoringService {
    
    private final ExamViolationRepository violationRepository;
    private final ExamSessionService examSessionService;
    private final AttemptService attemptService;

    @Value("${exam.proctoring.threshold.default:3}")
    private int defaultThreshold;

    @Value("${exam.proctoring.threshold.phone-detected:3}")
    private int phoneDetectedThreshold;

    @Value("${exam.proctoring.threshold.tab-switch:3}")
    private int tabSwitchThreshold;

    @Value("${exam.proctoring.threshold.looking-away:3}")
    private int lookingAwayThreshold;
    
    /**
     * Records a violation for an exam and applies business rules.
     *
     * Business Rules:
     * - Any violation type reaches threshold (default 3) -> auto-submit exam
     * - Otherwise -> issue warning
     */
    @Transactional
    public RecordViolationResponse recordViolation(Long examId, Long userId, Long attemptId,
                                                    ExamViolationType type, String severity, String details) {
        log.info("Recording violation - examId: {}, userId: {}, type: {}", examId, userId, type);

        // Parse severity, default LOW
        ViolationSeverity sev = ViolationSeverity.LOW;
        if (severity != null) {
            try { sev = ViolationSeverity.valueOf(severity.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        
        try {
            // Count existing violations to derive snapshot (new one not yet saved)
            int currentViolationCount = violationRepository.countByUserIdAndExamId(userId, examId).intValue() + 1;

            Long currentTypeCount = violationRepository.countByExamIdAndUserIdAndType(examId, userId, type);
            int thresholdForType = getThresholdForType(type);
            // +1 because the new violation hasn't been persisted yet at this point
            long newTypeCount = currentTypeCount + 1;

            // Determine the action taken
            String actionTaken;
            if (newTypeCount >= thresholdForType) {
                actionTaken = "AUTO_SUBMIT";
            } else {
                actionTaken = "WARNING";
            }

            // Create violation record
            ExamViolation violation = ExamViolation.builder()
                    .examId(examId)
                    .userId(userId)
                    .attemptId(attemptId)
                    .type(type)
                    .severity(sev)
                    .timestamp(LocalDateTime.now())
                    .details(details)
                    .actionTaken(actionTaken)
                    .countSnapshot(currentViolationCount)
                    .build();

            ExamViolation savedViolation = violationRepository.save(violation);
            log.debug("Violation saved successfully - violationId: {}", savedViolation.getId());

            // Apply business rules
            if (newTypeCount >= thresholdForType) {
                log.warn("Violation type threshold reached ({}) for {} - auto-submitting exam. ExamId: {}, UserId: {}",
                        thresholdForType, type, examId, userId);
                examSessionService.autoSubmitExam(attemptId);
                return RecordViolationResponse.builder()
                        .status(ViolationStatus.AUTO_SUBMITTED)
                        .violationCount(currentViolationCount)
                        .message("Exam auto-submitted: " + type + " reached "
                                + thresholdForType + " violations")
                        .action("AUTO_SUBMIT")
                        .build();
            }

            // Issue warning
            log.info("Violation recorded as warning. Current count: {}. {} count is {}/{}",
                    currentViolationCount, type, newTypeCount, thresholdForType);
            return RecordViolationResponse.builder()
                    .status(ViolationStatus.WARNING)
                    .violationCount(currentViolationCount)
                    .message("Violation recorded. You have " + Math.max(0, thresholdForType - newTypeCount)
                            + " remaining " + type + " warnings before auto-submission")
                    .action("CONTINUE")
                    .build();

        } catch (Exception e) {
            log.error("Error recording violation", e);
            throw new RuntimeException("Failed to record violation: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves all violations for a specific exam and user.
     */
    @Transactional(readOnly = true)
    public List<ViolationDTO> getViolationsByExamAndUser(Long examId, Long userId) {
        log.debug("Fetching violations - examId: {}, userId: {}", examId, userId);
        
        List<ExamViolation> violations = violationRepository.findByExamIdAndUserId(examId, userId);
        
        return violations.stream()
                .map(this::mapToViolationDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves violation count for exam and user.
     */
    @Transactional(readOnly = true)
    public Long getViolationCount(Long examId, Long userId) {
        log.debug("Fetching violation count - examId: {}, userId: {}", examId, userId);
        return violationRepository.countByUserIdAndExamId(userId, examId);
    }
    
    /**
     * Retrieves all violations for a specific exam.
     */
    @Transactional(readOnly = true)
    public List<ViolationDTO> getViolationsByExam(Long examId) {
        log.debug("Fetching violations for exam - examId: {}", examId);
        
        List<ExamViolation> violations = violationRepository.findByExamId(examId);
        
        return violations.stream()
                .map(this::mapToViolationDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves all violations for a specific attempt.
     */
    @Transactional(readOnly = true)
    public List<ViolationDTO> getViolationsByAttempt(Long attemptId) {
        return violationRepository.findByAttemptId(attemptId).stream()
                .map(this::mapToViolationDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns a summary (count + last action) for a given exam+user combination.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getViolationSummary(Long examId, Long userId) {
        List<ExamViolation> list = violationRepository.findByExamIdAndUserId(examId, userId);
        long total = list.size();
        String lastAction = list.isEmpty() ? null : list.get(list.size() - 1).getActionTaken();
        Map<String, Object> summary = new java.util.LinkedHashMap<>();
        summary.put("examId", examId);
        summary.put("userId", userId);
        summary.put("totalViolations", total);
        summary.put("lastAction", lastAction);
        return summary;
    }

    /**
     * Maps ExamViolation entity to ViolationDTO.
     */
    private ViolationDTO mapToViolationDTO(ExamViolation violation) {
        return ViolationDTO.builder()
                .id(violation.getId())
                .examId(violation.getExamId())
                .userId(violation.getUserId())
                .attemptId(violation.getAttemptId())
                .type(violation.getType())
                .severity(violation.getSeverity())
                .timestamp(violation.getTimestamp())
                .details(violation.getDetails())
                .actionTaken(violation.getActionTaken())
                .countSnapshot(violation.getCountSnapshot())
                .build();
    }
    
    private int getThresholdForType(ExamViolationType type) {
        return switch (type) {
            case PHONE_DETECTED -> phoneDetectedThreshold;
            case TAB_SWITCH -> tabSwitchThreshold;
            case LOOKING_AWAY -> lookingAwayThreshold;
            default -> defaultThreshold;
        };
    }
}
