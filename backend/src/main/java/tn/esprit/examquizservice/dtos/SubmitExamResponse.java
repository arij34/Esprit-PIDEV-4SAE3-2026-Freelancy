package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitExamResponse {
    @JsonProperty("attemptId")
    private Long attemptId;
    
    @JsonProperty("score")
    private Double score;
    
    @JsonProperty("totalPoints")
    private Double totalPoints;
    
    @JsonProperty("percentage")
    private Double percentage;
    
    @JsonProperty("passingScore")
    private Double passingScore;
    
    @JsonProperty("passed")
    private Boolean passed;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("suspiciousScore")
    private Double suspiciousScore;
    
    @JsonProperty("flagged")
    private Boolean flagged;

    @JsonProperty("autoSubmitted")
    private Boolean autoSubmitted;
    
    @JsonProperty("cheatingEventsCount")
    private Integer cheatingEventsCount;
    
    @JsonProperty("message")
    private String message;
}
