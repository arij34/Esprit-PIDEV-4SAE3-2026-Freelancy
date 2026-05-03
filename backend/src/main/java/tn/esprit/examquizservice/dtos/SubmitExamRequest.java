package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.Map;
import java.util.List;

@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitExamRequest {
    @JsonProperty("attemptId")
    private Long attemptId;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("examId")
    private Long examId;
    
    @JsonProperty("answers")
    private Map<Long, String> answers; // questionId -> answerText
    
    @JsonProperty("sessionToken")
    private String sessionToken;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("deviceFingerprint")
    private String deviceFingerprint;
    
    @JsonProperty("timeTakenSeconds")
    private Integer timeTakenSeconds;
    
    @JsonProperty("cheatingEvents")
    private List<CheatingEventDTO> cheatingEvents;
    
    @JsonProperty("autoSubmitted")
    private Boolean autoSubmitted;
}
