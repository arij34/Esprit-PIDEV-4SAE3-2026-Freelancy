package tn.esprit.examquizservice.utils;

import tn.esprit.examquizservice.dtos.RecordViolationRequest;
import tn.esprit.examquizservice.dtos.ViolationDTO;
import tn.esprit.examquizservice.entities.ExamViolationType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder utility for creating violation requests.
 * Provides a fluent API for constructing RecordViolationRequest objects.
 */
@Component
public class ViolationRequestBuilder {
    
    private final Map<String, Object> fields = new HashMap<>();
    
    public ViolationRequestBuilder examId(Long examId) {
        fields.put("examId", examId);
        return this;
    }
    
    public ViolationRequestBuilder userId(Long userId) {
        fields.put("userId", userId);
        return this;
    }
    
    public ViolationRequestBuilder type(ExamViolationType type) {
        fields.put("type", type);
        return this;
    }
    
    public ViolationRequestBuilder type(String typeString) {
        try {
            fields.put("type", ExamViolationType.valueOf(typeString.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid violation type: " + typeString);
        }
        return this;
    }
    
    public ViolationRequestBuilder details(String details) {
        fields.put("details", details);
        return this;
    }
    
    public RecordViolationRequest build() {
        return RecordViolationRequest.builder()
                .examId((Long) fields.get("examId"))
                .userId((Long) fields.get("userId"))
                .type((ExamViolationType) fields.get("type"))
                .details((String) fields.getOrDefault("details", null))
                .build();
    }
    
    public static ViolationRequestBuilder builder() {
        return new ViolationRequestBuilder();
    }
    
    public void reset() {
        fields.clear();
    }
}
