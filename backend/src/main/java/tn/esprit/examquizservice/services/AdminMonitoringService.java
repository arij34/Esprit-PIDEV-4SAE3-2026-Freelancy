package tn.esprit.examquizservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tn.esprit.examquizservice.dtos.AdminLiveCandidateDTO;
import tn.esprit.examquizservice.dtos.AdminLiveEventDTO;
import tn.esprit.examquizservice.dtos.AdminLiveExamSnapshotDTO;
import tn.esprit.examquizservice.entities.Attempt;
import tn.esprit.examquizservice.entities.AttemptStatus;
import tn.esprit.examquizservice.entities.CheatingLog;
import tn.esprit.examquizservice.entities.ExamParticipation;
import tn.esprit.examquizservice.entities.ExamViolation;
import tn.esprit.examquizservice.repositories.AttemptRepository;
import tn.esprit.examquizservice.repositories.CheatingLogRepository;
import tn.esprit.examquizservice.repositories.ExamParticipationRepository;
import tn.esprit.examquizservice.repositories.ExamRepository;
import tn.esprit.examquizservice.repositories.ExamViolationRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMonitoringService {

    private static final int DEFAULT_RECENT_EVENTS_LIMIT = 25;
    private static final int STREAM_INTERVAL_MS = 3000;

    /** Statuses included in the live monitoring view */
    private static final List<AttemptStatus> MONITORED_STATUSES =
            List.of(AttemptStatus.IN_PROGRESS, AttemptStatus.AUTO_SUBMITTED, AttemptStatus.SUBMITTED);

    private final AttemptRepository attemptRepository;
    private final ExamViolationRepository examViolationRepository;
    private final CheatingLogRepository cheatingLogRepository;
    private final ExamRepository examRepository;
    private final ExamParticipationRepository participationRepository;

    @Transactional(readOnly = true)
    public AdminLiveExamSnapshotDTO getLiveSnapshot(Long examId) {
        // Fetch exam title directly so it works even when no candidates are active
        String examTitle = examRepository.findById(examId)
                .map(e -> e.getTitle())
                .orElse(null);

        // Include IN_PROGRESS, AUTO_SUBMITTED and SUBMITTED so admins see every
        // candidate who participated in the current session, not only the ones
        // still clicking (especially important because phone-detection threshold=1
        // immediately flips status to AUTO_SUBMITTED)
        List<Attempt> allAttempts = attemptRepository.findByExamIdAndStatusIn(examId, MONITORED_STATUSES);

        List<Long> attemptIds = allAttempts.stream().map(Attempt::getId).toList();

        // Build userId→participation map so we can display real names in the admin UI
        List<Long> userIds = allAttempts.stream().map(Attempt::getUserId).distinct().toList();
        Map<Long, ExamParticipation> participationByUserId = userIds.isEmpty()
                ? Map.of()
                : participationRepository.findByExam_Id(examId).stream()
                        .collect(Collectors.toMap(ExamParticipation::getUserId, p -> p, (a, b) -> a));

        List<ExamViolation> violations = attemptIds.isEmpty()
                ? List.of()
                : examViolationRepository.findByExamIdAndAttemptIdIn(examId, attemptIds);
        List<CheatingLog> cheatingLogs = attemptIds.isEmpty()
                ? List.of()
                : cheatingLogRepository.findByAttemptIdIn(attemptIds);

        Map<Long, List<ExamViolation>> violationsByAttempt = violations.stream()
                .collect(Collectors.groupingBy(ExamViolation::getAttemptId));
        Map<Long, List<CheatingLog>> logsByAttempt = cheatingLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getAttempt().getId()));

        List<AdminLiveCandidateDTO> candidates = allAttempts.stream()
                .map(attempt -> mapCandidate(attempt,
                        violationsByAttempt.getOrDefault(attempt.getId(), List.of()),
                        logsByAttempt.getOrDefault(attempt.getId(), List.of()),
                        participationByUserId.get(attempt.getUserId())))
                .sorted(Comparator.comparing(AdminLiveCandidateDTO::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        long activeCandidates = allAttempts.stream()
                .filter(a -> AttemptStatus.IN_PROGRESS == a.getStatus()).count();

        return AdminLiveExamSnapshotDTO.builder()
                .examId(examId)
                .examTitle(examTitle)
                .activeCandidates((int) activeCandidates)
                .totalParticipants(candidates.size())
                .generatedAt(LocalDateTime.now())
                .candidates(candidates)
                .recentEvents(getRecentEvents(examId, 30, DEFAULT_RECENT_EVENTS_LIMIT))
                .build();
    }

    @Transactional(readOnly = true)
    public List<AdminLiveEventDTO> getRecentEvents(Long examId, int sinceMinutes, int limit) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(Math.max(1, sinceMinutes));
        int safeLimit = limit <= 0 ? DEFAULT_RECENT_EVENTS_LIMIT : limit;

        List<AdminLiveEventDTO> events = new ArrayList<>();

        examViolationRepository.findByExamIdAndTimestampAfterOrderByTimestampDesc(examId, since)
                .stream()
                .map(this::mapViolationEvent)
                .forEach(events::add);

        cheatingLogRepository.findRecentByExamId(examId, since)
                .stream()
                .map(this::mapCheatingEvent)
                .forEach(events::add);

        return events.stream()
                .sorted(Comparator.comparing(AdminLiveEventDTO::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(safeLimit)
                .toList();
    }

    /**
     * Returns live activity for one candidate in one exam.
     * active=true only when latest attempt status is IN_PROGRESS.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCandidateActivity(Long examId, Long userId) {
        String examTitle = examRepository.findById(examId)
                .map(e -> e.getTitle())
                .orElse(null);

        ExamParticipation participation = participationRepository
                .findByExam_IdAndUserId(examId, userId)
                .orElse(null);

        Attempt latestAttempt = attemptRepository.findByExamIdAndStatusIn(examId, MONITORED_STATUSES).stream()
                .filter(a -> userId.equals(a.getUserId()))
                .max(Comparator.comparing(Attempt::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        boolean active = latestAttempt != null && latestAttempt.getStatus() == AttemptStatus.IN_PROGRESS;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("examId", examId);
        payload.put("examTitle", examTitle);
        payload.put("userId", userId);
        payload.put("userFirstName", participation != null ? participation.getUserFirstName() : null);
        payload.put("userLastName", participation != null ? participation.getUserLastName() : null);
        payload.put("userEmail", participation != null ? participation.getUserEmail() : null);
        payload.put("attemptId", latestAttempt != null ? latestAttempt.getId() : null);
        payload.put("attemptStatus", latestAttempt != null && latestAttempt.getStatus() != null
                ? latestAttempt.getStatus().name()
                : "NOT_STARTED");
        payload.put("startedAt", latestAttempt != null ? latestAttempt.getStartTime() : null);
        payload.put("submittedAt", latestAttempt != null ? latestAttempt.getSubmittedAt() : null);
        payload.put("active", active);
        payload.put("generatedAt", LocalDateTime.now());

        return payload;
    }

    public SseEmitter streamExamMonitoring(Long examId) {
        // Long.MAX_VALUE = no timeout; 0L is ambiguous across servlet containers
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        AtomicBoolean active = new AtomicBoolean(true);

        emitter.onCompletion(() -> active.set(false));
        emitter.onTimeout(() -> {
            active.set(false);
            emitter.complete();
        });
        emitter.onError(error -> active.set(false));

        CompletableFuture.runAsync(() -> {
            try {
                while (active.get()) {
                    emitter.send(SseEmitter.event()
                            .name("snapshot")
                            .data(getLiveSnapshot(examId)));
                    Thread.sleep(STREAM_INTERVAL_MS);
                }
            } catch (IOException e) {
                log.debug("Admin monitoring stream closed for exam {}", examId);
                emitter.complete();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                emitter.complete();
            } catch (Exception e) {
                log.error("Admin monitoring stream failed for exam {}", examId, e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private AdminLiveCandidateDTO mapCandidate(Attempt attempt, List<ExamViolation> violations, List<CheatingLog> logs,
                                               ExamParticipation participation) {
                int answeredQuestions = attemptRepository.countAnsweredQuestions(attempt.getId());
        ExamViolation lastViolation = violations.stream()
                .max(Comparator.comparing(ExamViolation::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        LocalDateTime lastLogTime = logs.stream()
                .map(CheatingLog::getEventTime)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime lastViolationTime = lastViolation != null ? lastViolation.getTimestamp() : null;
        LocalDateTime lastActivityTime = StreamMax.max(lastLogTime, lastViolationTime, attempt.getSubmittedAt(), attempt.getStartTime());

        return AdminLiveCandidateDTO.builder()
                .attemptId(attempt.getId())
                .examId(attempt.getExam() != null ? attempt.getExam().getId() : null)
                .examTitle(attempt.getExam() != null ? attempt.getExam().getTitle() : null)
                .userId(attempt.getUserId())
                .userFirstName(participation != null ? participation.getUserFirstName() : null)
                .userLastName(participation != null ? participation.getUserLastName() : null)
                .userEmail(participation != null ? participation.getUserEmail() : null)
                .attemptStatus(attempt.getStatus() != null ? attempt.getStatus().name() : null)
                .startedAt(attempt.getStartTime())
                .expectedEndTime(attempt.getExpectedEndTime())
                .suspiciousScore(attempt.getSuspiciousScore())
                .answeredQuestions(answeredQuestions)
                .cheatingEventsCount(logs.size())
                .violationCount(violations.size())
                .lastWarningType(lastViolation != null && lastViolation.getType() != null ? lastViolation.getType().name() : null)
                .lastWarningAction(lastViolation != null ? lastViolation.getActionTaken() : null)
                .lastWarningTime(lastViolationTime)
                .lastActivityTime(lastActivityTime)
                .autoSubmitted(Boolean.TRUE.equals(attempt.getAutoSubmitted()))
                .riskLevel(computeRiskLevel(attempt.getSuspiciousScore(), violations.size()))
                .build();
    }

    private AdminLiveEventDTO mapViolationEvent(ExamViolation violation) {
        return AdminLiveEventDTO.builder()
                .source("VIOLATION")
                .examId(violation.getExamId())
                .attemptId(violation.getAttemptId())
                .userId(violation.getUserId())
                .type(violation.getType() != null ? violation.getType().name() : null)
                .severity(violation.getSeverity() != null ? violation.getSeverity().name() : null)
                .action(violation.getActionTaken())
                .details(violation.getDetails())
                .timestamp(violation.getTimestamp())
                .build();
    }

    private AdminLiveEventDTO mapCheatingEvent(CheatingLog cheatingLog) {
        Attempt attempt = cheatingLog.getAttempt();
        return AdminLiveEventDTO.builder()
                .source("CHEATING_LOG")
                .examId(attempt != null && attempt.getExam() != null ? attempt.getExam().getId() : null)
                .attemptId(attempt != null ? attempt.getId() : null)
                .userId(attempt != null ? attempt.getUserId() : null)
                .type(cheatingLog.getEventType() != null ? cheatingLog.getEventType().name() : null)
                .severity(null)
                .action(null)
                .details(cheatingLog.getDetails())
                .timestamp(cheatingLog.getEventTime())
                .build();
    }

    private String computeRiskLevel(Double suspiciousScore, int violationCount) {
        double score = suspiciousScore != null ? suspiciousScore : 0.0;
        if (score >= 5.0 || violationCount >= 3) return "HIGH";
        if (score >= 2.0 || violationCount >= 1) return "MEDIUM";
        return "LOW";
    }

    private static final class StreamMax {
        private static LocalDateTime max(LocalDateTime... values) {
            return java.util.Arrays.stream(values)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        }
    }
}