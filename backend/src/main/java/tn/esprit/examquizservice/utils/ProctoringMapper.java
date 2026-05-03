package tn.esprit.examquizservice.utils;

import tn.esprit.examquizservice.dtos.RecordViolationRequest;
import tn.esprit.examquizservice.entities.ExamViolationType;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility mapper for converting between DTOs and entities in the proctoring system.
 */
@Component
@Slf4j
public class ProctoringMapper {
    
    /**
     * Validates that the violation request contains required fields.
     */
    public static boolean isValidViolationRequest(RecordViolationRequest request) {
        return request != null 
            && request.getExamId() != null 
            && request.getUserId() != null 
            && request.getType() != null;
    }
    
    /**
     * Converts violation type string to enum.
     */
    public static ExamViolationType parseViolationType(String typeString) {
        try {
            return ExamViolationType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Invalid violation type: {}", typeString);
            return null;
        }
    }
}
