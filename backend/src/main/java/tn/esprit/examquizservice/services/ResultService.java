package tn.esprit.examquizservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.examquizservice.clients.UserServiceClient;
import tn.esprit.examquizservice.dtos.ResultResponse;
import tn.esprit.examquizservice.dtos.SubmitResultRequest;
import tn.esprit.examquizservice.entities.Exam;
import tn.esprit.examquizservice.entities.ExamResult;
import tn.esprit.examquizservice.entities.ResultStatus;
import tn.esprit.examquizservice.exceptions.DuplicateResultException;
import tn.esprit.examquizservice.exceptions.ResourceNotFoundException;
import tn.esprit.examquizservice.repositories.ExamRepository;
import tn.esprit.examquizservice.repositories.ExamResultRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultService {

    private final ExamResultRepository resultRepository;
    private final ExamRepository examRepository;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;

    /** POST /api/results/submit */
    public ResultResponse submit(SubmitResultRequest request) {
        // Reject duplicate: same attempt already has a result
        resultRepository.findByAttemptId(request.getAttemptId()).ifPresent(existing -> {
            throw new DuplicateResultException(
                    "Result already submitted for attemptId=" + request.getAttemptId());
        });

        // Reject duplicate: same (examId, userId) — enforces one-attempt-per-user at API level
        resultRepository.findByUserIdAndExamId(request.getUserId(), request.getExamId())
                .ifPresent(existing -> {
                    throw new DuplicateResultException(
                            "Exam already completed by userId=" + request.getUserId()
                                    + " for examId=" + request.getExamId());
                });

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found: " + request.getExamId()));

        double total   = request.getTotalPoints() != null ? request.getTotalPoints()
                       : (exam.getPoints() != null ? exam.getPoints() : 0);
        double earned  = request.getEarnedPoints() != null ? request.getEarnedPoints() : 0;
        double percent = total > 0 ? (earned / total) * 100.0 : 0.0;

        ResultStatus status;
        if (Boolean.TRUE.equals(request.getAutoSubmitted())) {
            status = ResultStatus.AUTO_SUBMITTED;
        } else {
            double passing = exam.getPassingScore() != null ? exam.getPassingScore() : 50.0;
            status = percent >= passing ? ResultStatus.PASSED : ResultStatus.FAILED;
        }

        String answersJson = null;
        if (request.getAnswers() != null) {
            try {
                answersJson = objectMapper.writeValueAsString(request.getAnswers());
            } catch (JsonProcessingException e) {
                answersJson = request.getAnswers().toString();
            }
        }

        ExamResult result = ExamResult.builder()
                .examId(request.getExamId())
                .userId(request.getUserId())
                .attemptId(request.getAttemptId())
                .scorePercent(percent)
                .earnedPoints(earned)
                .totalPoints(total)
                .status(status)
                .submittedAt(LocalDateTime.now())
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .answersJson(answersJson)
                .build();

        ExamResult saved = resultRepository.save(result);
        awardExperiencePoints(saved.getUserId(), saved.getEarnedPoints());
        return toResponse(saved);
    }

    /**
     * Internal use: auto-save result directly from ExamSessionService after submitExam.
     * Does NOT throw on duplicate — idempotent (returns existing if already saved).
     */
    public ResultResponse saveOrGet(Long examId, Long userId, Long attemptId, Double earned,
                                    Double total, Boolean autoSubmitted, Integer timeTakenSeconds,
                                    Double passingScore) {
        return resultRepository.findByAttemptId(attemptId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    double percent = total != null && total > 0 ? (earned / total) * 100.0 : 0.0;
                    ResultStatus status;
                    if (Boolean.TRUE.equals(autoSubmitted)) {
                        status = ResultStatus.AUTO_SUBMITTED;
                    } else {
                        double passing = passingScore != null ? passingScore : 50.0;
                        status = percent >= passing ? ResultStatus.PASSED : ResultStatus.FAILED;
                    }
                    ExamResult result = ExamResult.builder()
                            .examId(examId).userId(userId).attemptId(attemptId)
                            .scorePercent(percent).earnedPoints(earned).totalPoints(total)
                            .status(status).submittedAt(LocalDateTime.now())
                            .timeTakenSeconds(timeTakenSeconds)
                            .build();
                    ExamResult saved = resultRepository.save(result);
                    awardExperiencePoints(saved.getUserId(), saved.getEarnedPoints());
                    return toResponse(saved);
                });
    }

    /** GET /api/results/me?examId=... */
    public ResultResponse getMyResult(Long userId, Long examId) {
        ExamResult result = resultRepository.findByUserIdAndExamId(userId, examId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No result found for userId=" + userId + " examId=" + examId));
        return toResponse(result);
    }

    /** GET /api/results/by-attempt/{attemptId} */
    public ResultResponse getByAttempt(Long attemptId) {
        ExamResult result = resultRepository.findByAttemptId(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No result found for attemptId=" + attemptId));
        return toResponse(result);
    }

    /** GET /api/results/history/me */
    public List<ResultResponse> getHistory(Long userId) {
        return resultRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** GET /api/results?examId=&userId= (admin) */
    public List<ResultResponse> getByExamAndUser(Long examId, Long userId) {
        return resultRepository.findAllByUserIdAndExamId(userId, examId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Increases earned points for a user's result.
     * - If examId is provided: updates that exam result for the user.
     * - If examId is null: updates the latest result for that user.
     */
    @Transactional
    public ResultResponse increaseScoreByUserId(Long userId, Double deltaPoints, Long examId) {
        if (deltaPoints == null || deltaPoints <= 0) {
            throw new IllegalArgumentException("deltaPoints must be > 0");
        }

        ExamResult result = (examId != null)
                ? resultRepository.findByUserIdAndExamId(userId, examId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No result found for userId=" + userId + " examId=" + examId))
                : resultRepository.findTopByUserIdOrderBySubmittedAtDescIdDesc(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No result found for userId=" + userId));

        double currentEarned = result.getEarnedPoints() != null ? result.getEarnedPoints() : 0.0;
        double total = result.getTotalPoints() != null ? result.getTotalPoints() : 0.0;
        double updatedEarned = currentEarned + deltaPoints;

        // Keep earned points in a valid range.
        if (total > 0) {
            updatedEarned = Math.min(updatedEarned, total);
        }

        result.setEarnedPoints(updatedEarned);
        result.setScorePercent(total > 0 ? (updatedEarned / total) * 100.0 : updatedEarned);

        // Preserve AUTO_SUBMITTED semantics; otherwise recompute pass/fail.
        if (result.getStatus() != ResultStatus.AUTO_SUBMITTED) {
            double passingScore = examRepository.findById(result.getExamId())
                    .map(Exam::getPassingScore)
                    .orElse(50.0);
            result.setStatus(result.getScorePercent() >= passingScore ? ResultStatus.PASSED : ResultStatus.FAILED);
        }

        return toResponse(resultRepository.save(result));
    }

    /**
     * Calls the user service to add {@code earnedPoints} to the user's experiencePoints.
     * Failures are logged but never propagate — exam submission must succeed regardless.
     */
    private void awardExperiencePoints(Long userId, Double earnedPoints) {
        if (userId == null || earnedPoints == null || earnedPoints <= 0) return;
        try {
            userServiceClient.addExperiencePoints(userId, earnedPoints);
            log.info("Awarded {} XP to userId={}", earnedPoints, userId);
        } catch (Exception e) {
            log.warn("Failed to award XP to userId={}: {}", userId, e.getMessage());
        }
    }

    public ResultResponse toResponse(ExamResult r) {
        return ResultResponse.builder()
                .id(r.getId())
                .examId(r.getExamId())
                .userId(r.getUserId())
                .attemptId(r.getAttemptId())
                .scorePercent(r.getScorePercent())
                .earnedPoints(r.getEarnedPoints())
                .totalPoints(r.getTotalPoints())
                .status(r.getStatus())
                .submittedAt(r.getSubmittedAt())
                .timeTakenSeconds(r.getTimeTakenSeconds())
                .answersJson(r.getAnswersJson())
                .build();
    }
}
