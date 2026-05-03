package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.dtos.RecordViolationRequest;
import tn.esprit.examquizservice.dtos.RecordViolationResponse;
import tn.esprit.examquizservice.dtos.ViolationDTO;
import tn.esprit.examquizservice.services.ProctoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for exam proctoring and violation management.
 * 
 * Endpoints:
 * - POST /api/proctoring/violations - Record a new violation
 * - GET /api/proctoring/violations - Get violations for exam and user
 */
@RestController
@RequestMapping("/api/proctoring")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ProctoringController {

    private final ProctoringService proctoringService;

    @PostMapping("/violations")
    public ResponseEntity<?> recordViolation(
            @Valid @RequestBody RecordViolationRequest request,
            BindingResult bindingResult) {

        log.info("Recording violation - examId: {}, userId: {}, type: {}",
                request.getExamId(), request.getUserId(), request.getType());

        // Validate request body
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));

            log.warn("Validation error in violation request: {}", errors);
            return ResponseEntity.badRequest()
                    .body(RecordViolationResponse.builder()
                            .message("Validation error: " + errors)
                            .build());
        }

        try {
            RecordViolationResponse response = proctoringService.recordViolation(
                    request.getExamId(),
                    request.getUserId(),
                    request.getAttemptId(),
                    request.getType(),
                    request.getSeverity(),
                    request.getDetails()
            );

            log.info("Violation recorded successfully - status: {}, count: {}",
                    response.getStatus(), response.getViolationCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error recording violation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RecordViolationResponse.builder()
                            .message("Error recording violation: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Retrieves violations for a specific exam and user.
     * <p>
     * Query Parameters:
     * - examId (required): The exam ID
     * - userId (required): The user ID
     * <p>
     * Response: Array of ViolationDTO
     */
    @GetMapping("/violations")
    public ResponseEntity<?> getViolations(
            @RequestParam(required = true) Long examId,
            @RequestParam(required = true) Long userId) {

        log.debug("Fetching violations - examId: {}, userId: {}", examId, userId);

        try {
            List<ViolationDTO> violations = proctoringService.getViolationsByExamAndUser(examId, userId);

            log.info("Violations fetched successfully - count: {}", violations.size());
            return ResponseEntity.ok(violations);

        } catch (Exception e) {
            log.error("Error fetching violations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching violations: " + e.getMessage());
        }
    }

    /**
     * Retrieves violation count for a specific exam and user.
     * <p>
     * Query Parameters:
     * - examId (required): The exam ID
     * - userId (required): The user ID
     * <p>
     * Response: { "violationCount": number }
     */
    @GetMapping("/violations/count")
    public ResponseEntity<?> getViolationCount(
            @RequestParam(required = true) Long examId,
            @RequestParam(required = true) Long userId) {

        log.debug("Fetching violation count - examId: {}, userId: {}", examId, userId);

        try {
            Long count = proctoringService.getViolationCount(examId, userId);

            return ResponseEntity.ok()
                    .body(java.util.Map.of("violationCount", count));

        } catch (Exception e) {
            log.error("Error fetching violation count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching violation count: " + e.getMessage());
        }
    }

    /**
     * Retrieves all violations for a specific exam.
     * <p>
     * Query Parameters:
     * - examId (required): The exam ID
     * <p>
     * Response: Array of ViolationDTO
     */
    @GetMapping("/violations/exam")
    public ResponseEntity<?> getExamViolations(
            @RequestParam(required = true) Long examId) {

        log.debug("Fetching violations for exam - examId: {}", examId);

        try {
            List<ViolationDTO> violations = proctoringService.getViolationsByExam(examId);

            log.info("Exam violations fetched successfully - count: {}", violations.size());
            return ResponseEntity.ok(violations);

        } catch (Exception e) {
            log.error("Error fetching exam violations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching exam violations: " + e.getMessage());
        }
    }

    /**
     * GET /api/proctoring/violations/by-attempt/{attemptId}
     * Returns all violations tied to a specific attempt.
     */
    @GetMapping("/violations/by-attempt/{attemptId}")
    public ResponseEntity<?> getByAttempt(@PathVariable Long attemptId) {
        try {
            return ResponseEntity.ok(proctoringService.getViolationsByAttempt(attemptId));
        } catch (Exception e) {
            log.error("Error fetching violations by attempt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching violations: " + e.getMessage());
        }
    }

    /**
     * GET /api/proctoring/violations/summary?examId=&userId=
     * Returns aggregated violation summary for a user on an exam.
     */
    @GetMapping("/violations/summary")
    public ResponseEntity<?> getViolationSummary(
            @RequestParam Long examId,
            @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(proctoringService.getViolationSummary(examId, userId));
        } catch (Exception e) {
            log.error("Error fetching violation summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching summary: " + e.getMessage());

        }
    }
}

