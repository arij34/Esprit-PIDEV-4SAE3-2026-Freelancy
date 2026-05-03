package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.dtos.*;
import tn.esprit.examquizservice.services.ExamSessionService;
import tn.esprit.examquizservice.services.AntiCheatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam-sessions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ExamSessionController {
    
    private final ExamSessionService examSessionService;
    private final AntiCheatingService antiCheatingService;
    
    /**
     * Start a new exam session
     */
    @PostMapping("/start")
    public ResponseEntity<StartExamResponse> startExam(@RequestBody StartExamRequest request) {
        log.info("Starting exam session for user {} on exam {}", request.getUserId(), request.getExamId());
        try {
            StartExamResponse response = examSessionService.startExam(request);
            if (response.getAttemptId() != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error starting exam session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StartExamResponse.builder()
                    .message("Error starting exam session: " + e.getMessage())
                    .build());
        }
    }
    
    /**
     * Submit exam with all answers and cheating events
     */
    @PostMapping("/submit")
    public ResponseEntity<SubmitExamResponse> submitExam(@RequestBody SubmitExamRequest request) {
        log.info("Submitting exam for attempt {}", request.getAttemptId());
        try {
            SubmitExamResponse response = examSessionService.submitExam(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error submitting exam", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SubmitExamResponse.builder()
                    .message("Error submitting exam: " + e.getMessage())
                    .build());
        }
    }
    
    /**
     * Save individual answer (used for continuous auto-save)
     */
    @PostMapping("/save-answer")
    public ResponseEntity<String> saveAnswer(@RequestBody SaveAnswerRequest request) {
        log.debug("Saving answer for attempt {}, question {}", request.getAttemptId(), request.getQuestionId());
        try {
            // Validate session token
            if (!antiCheatingService.validateSessionToken(
                request.getSessionToken(), 
                request.getIpAddress(), 
                request.getDeviceFingerprint())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired session token");
            }
            
            examSessionService.saveAnswer(
                request.getAttemptId(),
                request.getQuestionId(),
                request.getAnswerText(),
                request.getTimeTakenSeconds()
            );
            
            return ResponseEntity.ok("Answer saved successfully");
        } catch (Exception e) {
            log.error("Error saving answer", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error saving answer: " + e.getMessage());
        }
    }
    
    /**
     * Report a cheating event (tab switch, fullscreen exit, etc.)
     */
    @PostMapping("/report-event")
    public ResponseEntity<String> reportCheatingEvent(@RequestBody CheatingEventDTO event) {
        log.warn("Cheating event reported for attempt {}: {}", event.getAttemptId(), event.getEventType());
        try {
            // Validate session token
            if (!antiCheatingService.validateSessionToken(
                event.getSessionToken(), 
                event.getIpAddress(), 
                event.getDeviceFingerprint())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired session token");
            }
            
            antiCheatingService.recordCheatingEvent(event.getAttemptId(), event);
            
            return ResponseEntity.ok("Event recorded successfully");
        } catch (Exception e) {
            log.error("Error reporting cheating event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error reporting event: " + e.getMessage());
        }
    }
    
    /**
     * Check IP address change
     */
    @PostMapping("/validate-ip")
    public ResponseEntity<String> validateIp(
        @RequestParam Long attemptId,
        @RequestParam String ipAddress,
        @RequestParam String sessionToken) {
        
        log.debug("Validating IP for attempt {}", attemptId);
        try {
            // Validate session token
            if (!antiCheatingService.validateSessionToken(sessionToken, ipAddress, "")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired session token");
            }
            
            return ResponseEntity.ok("IP validation passed");
        } catch (Exception e) {
            log.error("Error validating IP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error validating IP: " + e.getMessage());
        }
    }
    
    /**
     * Auto-submit exam when time expires
     */
    @PostMapping("/auto-submit/{attemptId}")
    public ResponseEntity<SubmitExamResponse> autoSubmitExam(
        @PathVariable Long attemptId,
        @RequestParam String sessionToken) {
        
        log.info("Auto-submitting exam for attempt {}", attemptId);
        try {
            SubmitExamResponse response = examSessionService.autoSubmitExam(attemptId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error auto-submitting exam", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(SubmitExamResponse.builder()
                    .message("Error auto-submitting exam: " + e.getMessage())
                    .build());
        }
    }

}
