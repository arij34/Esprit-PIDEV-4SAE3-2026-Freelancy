package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.*;
import tn.esprit.examquizservice.repositories.AttemptRepository;
import tn.esprit.examquizservice.repositories.CheatingLogRepository;
import tn.esprit.examquizservice.repositories.ExamRepository;
import tn.esprit.examquizservice.repositories.ExamSettingRepository;
import tn.esprit.examquizservice.dtos.StartExamRequest;
import tn.esprit.examquizservice.dtos.StartExamResponse;
import tn.esprit.examquizservice.dtos.SubmitExamRequest;
import tn.esprit.examquizservice.dtos.SubmitExamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamSessionService {
    
    private final AttemptRepository attemptRepository;
    private final ExamRepository examRepository;
    private final ExamSettingRepository examSettingRepository;
    private final ResultService resultService;
    private final AntiCheatingService antiCheatingService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final AttemptAnswerService attemptAnswerService;
    private final CheatingLogRepository cheatingLogRepository;
    
    @Value("${exam.rate-limit.requests:10}")
    private int rateLimitRequests;
    
    @Value("${exam.rate-limit.minutes:5}")
    private int rateLimitMinutes;
    
    /**
     * Start a new exam session
     */
    public StartExamResponse startExam(StartExamRequest request) {
        List<Attempt> userAttempts = attemptRepository.findByUserId(request.getUserId());
        long nowMs = System.currentTimeMillis();

        // Clean stale in-progress attempts that have already timed out.
        userAttempts.stream()
            .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS)
            .filter(a -> a.getExpectedEndTime() != null && a.getExpectedEndTime() < nowMs)
            .forEach(a -> {
                a.setStatus(AttemptStatus.AUTO_SUBMITTED);
                a.setAutoSubmitted(true);
                a.setEndTime(LocalDateTime.now());
                a.setSubmittedAt(LocalDateTime.now());
                attemptRepository.save(a);
                log.info("Marked stale in-progress attempt {} as AUTO_SUBMITTED before startExam", a.getId());
            });

        // Re-read after cleanup to decide whether we can start/reuse.
        userAttempts = attemptRepository.findByUserId(request.getUserId());

        Optional<Attempt> activeAttemptForExam = userAttempts.stream()
            .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS)
            .filter(a -> a.getExam() != null && request.getExamId().equals(a.getExam().getId()))
            .max(java.util.Comparator.comparing(Attempt::getStartTime, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));

        if (activeAttemptForExam.isPresent()) {
            Attempt existing = activeAttemptForExam.get();
            Optional<Exam> existingExam = examRepository.findById(request.getExamId());
            int durationMinutes = existingExam.map(Exam::getDuration).orElse(60);
            double totalPoints = existingExam.map(Exam::getPoints).orElse(0.0);
            int questionCount = existingExam.map(e -> questionService.findByExamId(e.getId()).size()).orElse(0);

            log.info("User {} already has active session for exam {}. Reusing attempt {}",
                request.getUserId(), request.getExamId(), existing.getId());

            return StartExamResponse.builder()
                .attemptId(existing.getId())
                .sessionToken(existing.getSessionToken())
                .examId(request.getExamId())
                .userId(request.getUserId())
                .examTitle(existingExam.map(Exam::getTitle).orElse(null))
                .durationMinutes(durationMinutes)
                .startTime(existing.getStartTime() != null
                    ? existing.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    : System.currentTimeMillis())
                .expectedEndTime(existing.getExpectedEndTime())
                .totalPoints(totalPoints)
                .questionCount(questionCount)
                .message("Active exam session resumed")
                .build();
        }

        boolean hasOtherActiveExam = userAttempts.stream()
            .anyMatch(a -> a.getStatus() == AttemptStatus.IN_PROGRESS
                && (a.getExam() == null || !request.getExamId().equals(a.getExam().getId())));

        if (hasOtherActiveExam) {
            log.warn("User {} already has an active exam session on another exam", request.getUserId());
            return StartExamResponse.builder()
                .message("You already have an active exam session. Please submit or finish it first.")
                .build();
        }
        
        Optional<Exam> exam = examRepository.findById(request.getExamId());
        if (exam.isEmpty()) {
            log.error("Exam not found: {}", request.getExamId());
            return StartExamResponse.builder()
                .message("Exam not found")
                .build();
        }
        
        Exam examData = exam.get();

        // One-attempt-per-user guard
        ExamSetting setting = examSettingRepository.findByExam_Id(examData.getId()).orElse(null);
        if (setting != null && Boolean.TRUE.equals(setting.getOneAttemptPerUser())) {
            boolean alreadyDone = resultService.getHistory(request.getUserId()).stream()
                    .anyMatch(r -> examData.getId().equals(r.getExamId()));
            if (alreadyDone) {
                log.warn("User {} already completed exam {} (oneAttemptPerUser=true)",
                        request.getUserId(), examData.getId());
                return StartExamResponse.builder()
                        .message("Exam already completed. Only one attempt is allowed.")
                        .build();
            }
        }

        // Check if user has exceeded max attempts
        List<Attempt> previousAttempts = attemptRepository.findByUserId(request.getUserId());
        if (examData.getMaxAttempts() != null && 
            previousAttempts.stream().filter(a -> a.getExam().getId().equals(examData.getId())).count() >= examData.getMaxAttempts()) {
            log.warn("User {} exceeded max attempts for exam {}", request.getUserId(), examData.getId());
            return StartExamResponse.builder()
                .message("Maximum attempts exceeded for this exam")
                .build();
        }
        
        // Create new attempt
        long now = System.currentTimeMillis();
        long durationMillis = (examData.getDuration() != null ? examData.getDuration() : 60) * 60 * 1000L;
        
        String sessionToken = UUID.randomUUID().toString();
        String normalizedDeviceFingerprint = normalizeForVarchar(request.getDeviceFingerprint(), 255);
        
        Attempt attempt = Attempt.builder()
            .userId(request.getUserId())
            .exam(examData)
            .startTime(LocalDateTime.now())
            .status(AttemptStatus.IN_PROGRESS)
            .ipAddress(request.getIpAddress())
            .deviceFingerprint(normalizedDeviceFingerprint)
            .browserInfo(request.getBrowserInfo())
            .sessionToken(sessionToken)
            .expectedEndTime(now + durationMillis)
            .suspiciousScore(0.0)
            .tabSwitchCount(0)
            .fullscreenExitCount(0)
            .answerChangeCount(0)
            .tooFastAnswerCount(0)
            .autoSubmitted(false)
            .build();
        
        Attempt savedAttempt = attemptRepository.save(attempt);
        
        // Get exam questions for question count
        List<Question> questions = questionService.findByExamId(examData.getId());
        
        log.info("Started exam session for user {} on exam {} with attempt {}", 
            request.getUserId(), examData.getId(), savedAttempt.getId());
        
        return StartExamResponse.builder()
            .attemptId(savedAttempt.getId())
            .sessionToken(sessionToken)
            .examId(examData.getId())
            .userId(request.getUserId())
            .examTitle(examData.getTitle())
            .durationMinutes(examData.getDuration())
            .startTime(now)
            .expectedEndTime(now + durationMillis)
            .totalPoints(examData.getPoints())
            .questionCount(questions.size())
            .message("Exam session started successfully")
            .build();
    }

    private String normalizeForVarchar(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
    
    /**
     * Submit exam with all answers
     */
    @Transactional
    public SubmitExamResponse submitExam(SubmitExamRequest request) {
        // Validate token — log warning but NEVER block; result must always be persisted
        boolean tokenValid = antiCheatingService.validateSessionToken(
            request.getSessionToken(),
            request.getIpAddress(),
            request.getDeviceFingerprint());
        if (!tokenValid) {
            log.warn("Invalid or expired session token for attempt {} — will mark AUTO_SUBMITTED",
                request.getAttemptId());
        }

        Optional<Attempt> attempt = resolveAttemptForSubmit(request);
        if (attempt.isEmpty()) {
            log.error("Attempt not found for submit. attemptId={}, userId={}, examId={}",
                request.getAttemptId(), request.getUserId(), request.getExamId());
            return SubmitExamResponse.builder()
                .message("Attempt not found")
                .build();
        }
        
        Attempt att = attempt.get();
        
        // Process cheating events
        if (request.getCheatingEvents() != null) {
            request.getCheatingEvents().forEach(event -> {
                antiCheatingService.recordCheatingEvent(att.getId(), event);
            });
        }
        
        // Check if submitted after timeout
        boolean timedOut = att.getExpectedEndTime() != null && att.getExpectedEndTime() < System.currentTimeMillis();
        if (!tokenValid || timedOut) {
            att.setAutoSubmitted(true);
            att.setStatus(AttemptStatus.AUTO_SUBMITTED);
            log.warn("Attempt {} marked AUTO_SUBMITTED (tokenValid={}, timedOut={})",
                att.getId(), tokenValid, timedOut);
        } else {
            att.setStatus(AttemptStatus.SUBMITTED);
        }
        
        // Save answers
        if (request.getAnswers() != null) {
            saveAnswers(att.getId(), request.getAnswers());
        }
        
        att.setEndTime(LocalDateTime.now());
        att.setSubmittedAt(LocalDateTime.now());
        
        // Calculate score and suspicious score
        Double score = calculateScore(att);
        att.setScore(score);
        antiCheatingService.calculateFinalSuspiciousScore(att);
        
        Attempt finalAttempt = attemptRepository.save(att);
        
        log.info("Exam submitted for attempt {}, score: {}, suspicious score: {}", 
            finalAttempt.getId(), score, finalAttempt.getSuspiciousScore());
        
        // Get exam passing score
        Exam exam = att.getExam();
        Boolean passed = score >= exam.getPassingScore();
        
        List<CheatingLog> logs = cheatingLogRepository.findByAttemptId(att.getId());

        // Auto-persist result so backend is source of truth
        resultService.saveOrGet(
                exam.getId(), att.getUserId(), finalAttempt.getId(),
                score, exam.getPoints(),
                finalAttempt.getStatus() == AttemptStatus.AUTO_SUBMITTED,
                request.getTimeTakenSeconds(),
                exam.getPassingScore());

        return SubmitExamResponse.builder()
            .attemptId(finalAttempt.getId())
            .score(score)
            .totalPoints(exam.getPoints())
            .percentage((score / exam.getPoints()) * 100)
            .passingScore(exam.getPassingScore())
            .passed(passed)
            .status(finalAttempt.getStatus().toString())
            .suspiciousScore(finalAttempt.getSuspiciousScore())
            .flagged(finalAttempt.getStatus() == AttemptStatus.AUTO_SUBMITTED)
            .cheatingEventsCount(logs.size())
            .message("Exam submitted successfully")
            .build();
    }

    private Optional<Attempt> resolveAttemptForSubmit(SubmitExamRequest request) {
        if (request == null) {
            return Optional.empty();
        }

        if (request.getAttemptId() != null && request.getAttemptId() > 0) {
            Optional<Attempt> byId = attemptRepository.findById(request.getAttemptId());
            if (byId.isPresent()) {
                return byId;
            }
        }

        String token = request.getSessionToken();
        if (token != null && !token.isBlank()) {
            Optional<Attempt> byToken = attemptRepository.findBySessionToken(token);
            if (byToken.isPresent()) {
                return byToken;
            }
        }

        if (request.getUserId() != null && request.getExamId() != null) {
            return attemptRepository.findByUserIdAndExamIdAndStatus(
                request.getUserId(), request.getExamId(), AttemptStatus.IN_PROGRESS);
        }

        return Optional.empty();
    }
    
    /**
     * Save individual answer
     */
    public void saveAnswer(Long attemptId, Long questionId, String answerText, Integer timeTaken) {
        Optional<Attempt> attempt = attemptRepository.findById(attemptId);
        if (attempt.isEmpty()) {
            log.error("Attempt not found: {}", attemptId);
            return;
        }
        
        Attempt att = attempt.get();
        
        // Check if answer was too fast
        if (antiCheatingService.checkTooFastAnswer(timeTaken)) {
            att.setTooFastAnswerCount((att.getTooFastAnswerCount() != null ? att.getTooFastAnswerCount() : 0) + 1);
        }
        
        // Create or update attempt answer
        AttemptAnswer attemptAnswer = new AttemptAnswer();
        attemptAnswer.setAttempt(att);
        attemptAnswer.setAnsweredAt(LocalDateTime.now());
        
        Optional<Question> question = questionService.getById(questionId);
        if (question.isPresent()) {
            attemptAnswer.setQuestion(question.get());
            attemptAnswer.setTextAnswer(answerText);
            attemptAnswerService.create(attemptAnswer);
        }
        
        attemptRepository.save(att);
    }
    
    /**
     * Auto-submit exam when time expires
     */
    @Transactional
    public SubmitExamResponse autoSubmitExam(Long attemptId) {
        Optional<Attempt> attempt = attemptRepository.findById(attemptId);
        if (attempt.isEmpty()) {
            return SubmitExamResponse.builder()
                .message("Attempt not found")
                .build();
        }
        
        Attempt att = attempt.get();
        att.setStatus(AttemptStatus.AUTO_SUBMITTED);
        att.setAutoSubmitted(true);
        att.setEndTime(LocalDateTime.now());
        att.setSubmittedAt(LocalDateTime.now());
        
        Double score = calculateScore(att);
        att.setScore(score);
        
        Attempt finalAttempt = attemptRepository.save(att);
        
        Exam exam = att.getExam();
        Boolean passed = score >= exam.getPassingScore();

        // Auto-persist result so backend is source of truth
        resultService.saveOrGet(
                exam.getId(), att.getUserId(), finalAttempt.getId(),
                score, exam.getPoints(), true, null, exam.getPassingScore());

        log.info("Auto-submitted exam for attempt {}", attemptId);
        
        return SubmitExamResponse.builder()
            .attemptId(finalAttempt.getId())
            .score(score)
            .totalPoints(exam.getPoints())
            .percentage((score / exam.getPoints()) * 100)
            .passingScore(exam.getPassingScore())
            .passed(passed)
            .status("AUTO_SUBMITTED")
            .autoSubmitted(true)
            .message("Exam auto-submitted due to time expiration")
            .build();
    }
    
    // Helper methods
    
    private void saveAnswers(Long attemptId, java.util.Map<Long, String> answers) {
        answers.forEach((questionId, answerText) -> {
            saveAnswer(attemptId, questionId, answerText, null);
        });
    }
    
    private Double calculateScore(Attempt attempt) {
        // Implementation would depend on your scoring logic
        // For now, return a placeholder
        double score = 0.0;
        
        if (attempt.getAttemptAnswers() != null) {
            for (AttemptAnswer aa : attempt.getAttemptAnswers()) {
                // Score calculation logic here
                if (aa.getQuestion() != null && aa.getQuestion().getPoints() != null) {
                    score += aa.getQuestion().getPoints();
                }
            }
        }
        
        return score;
    }

}
