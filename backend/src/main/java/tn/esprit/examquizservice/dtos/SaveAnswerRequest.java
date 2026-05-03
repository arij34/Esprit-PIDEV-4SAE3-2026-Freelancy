package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveAnswerRequest {
    @JsonProperty("attemptId")
    private Long attemptId;
    
    @JsonProperty("questionId")
    private Long questionId;
    
    @JsonProperty("answerText")
    private String answerText;
    
    @JsonProperty("sessionToken")
    private String sessionToken;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("deviceFingerprint")
    private String deviceFingerprint;
    
    @JsonProperty("timeTakenSeconds")
    private Integer timeTakenSeconds;
}
