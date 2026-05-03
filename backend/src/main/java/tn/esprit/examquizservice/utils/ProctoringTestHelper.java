package tn.esprit.examquizservice.utils;

import tn.esprit.examquizservice.dtos.RecordViolationRequest;
import tn.esprit.examquizservice.dtos.RecordViolationResponse;
import tn.esprit.examquizservice.entities.ExamViolationType;
import lombok.extern.slf4j.Slf4j;

/**
 * Test and integration helper utility for the proctoring system.
 * Provides sample violation requests and response validators.
 */
@Slf4j
public class ProctoringTestHelper {
    
    /**
     * Creates a sample violation request for testing.
     */
    public static RecordViolationRequest createSampleViolation(Long examId, Long userId, ExamViolationType type) {
        return RecordViolationRequest.builder()
                .examId(examId)
                .userId(userId)
                .type(type)
                .details("Test violation - " + type.name())
                .build();
    }
    
    /**
     * Validates a RecordViolationResponse.
     */
    public static boolean isValidResponse(RecordViolationResponse response) {
        return response != null 
            && response.getStatus() != null 
            && response.getViolationCount() != null 
            && response.getMessage() != null;
    }
    
    /**
     * Logs violation response details.
     */
    public static void logViolationResponse(RecordViolationResponse response) {
        log.info("Violation Response - Status: {}, ViolationCount: {}, Action: {}, Message: {}", 
                response.getStatus(), 
                response.getViolationCount(), 
                response.getAction(), 
                response.getMessage());
    }
    
    /**
     * Sample test data for common violation types.
     */
    public enum SampleViolations {
        PHONE_DETECTED(ExamViolationType.PHONE_DETECTED, "Phone detected in camera feed"),
        MULTIPLE_PEOPLE(ExamViolationType.MULTIPLE_PEOPLE, "Multiple faces detected in frame"),
        LOOKING_AWAY(ExamViolationType.LOOKING_AWAY, "Student looking away from screen"),
        NO_FACE(ExamViolationType.NO_FACE, "No face detected in camera"),
        TAB_SWITCH(ExamViolationType.TAB_SWITCH, "Browser tab switch detected"),
        EXIT_FULLSCREEN(ExamViolationType.EXIT_FULLSCREEN, "Fullscreen mode exited"),
        SUSPICIOUS_MOVEMENT(ExamViolationType.SUSPICIOUS_MOVEMENT, "Suspicious movement detected");
        
        private final ExamViolationType type;
        private final String description;
        
        SampleViolations(ExamViolationType type, String description) {
            this.type = type;
            this.description = description;
        }
        
        public ExamViolationType getType() {
            return type;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
