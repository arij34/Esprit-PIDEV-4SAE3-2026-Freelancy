package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartExamRequest {
    private Long userId;
    private Long examId;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("deviceFingerprint")
    private String deviceFingerprint;
    
    @JsonProperty("browserInfo")
    private String browserInfo;
    
    @JsonProperty("screenResolution")
    private String screenResolution;
    
    @JsonProperty("timezone")
    private String timezone;
}
