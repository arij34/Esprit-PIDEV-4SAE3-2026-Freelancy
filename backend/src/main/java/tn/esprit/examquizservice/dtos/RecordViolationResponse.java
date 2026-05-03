package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordViolationResponse {
    
    @JsonProperty("status")
    private ViolationStatus status; // WARNING, TERMINATED, AUTO_SUBMITTED
    
    @JsonProperty("violationCount")
    private Integer violationCount;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("action")
    private String action; // e.g., "CONTINUE", "TERMINATE_EXAM", "AUTO_SUBMIT"
}
