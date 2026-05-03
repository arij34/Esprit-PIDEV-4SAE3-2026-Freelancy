package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartExamResponse {
    @JsonProperty("attemptId")
    private Long attemptId;
    
    @JsonProperty("sessionToken")
    private String sessionToken;
    
    @JsonProperty("examId")
    private Long examId;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("examTitle")
    private String examTitle;
    
    @JsonProperty("durationMinutes")
    private Integer durationMinutes;
    
    @JsonProperty("startTime")
    private Long startTime;
    
    @JsonProperty("expectedEndTime")
    private Long expectedEndTime;
    
    @JsonProperty("totalPoints")
    private Double totalPoints;
    
    @JsonProperty("questionCount")
    private Integer questionCount;
    
    @JsonProperty("message")
    private String message;
}
