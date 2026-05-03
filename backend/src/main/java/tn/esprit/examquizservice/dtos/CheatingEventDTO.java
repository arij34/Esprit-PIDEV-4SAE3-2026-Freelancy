package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import tn.esprit.examquizservice.entities.CheatingEventType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheatingEventDTO {
    @JsonProperty("attemptId")
    private Long attemptId;
    
    @JsonProperty("eventType")
    private CheatingEventType eventType;
    
    @JsonProperty("details")
    private String details;
    
    @JsonProperty("severity")
    private String severity; // LOW, MEDIUM, HIGH
    
    @JsonProperty("sessionToken")
    private String sessionToken;
    
    @JsonProperty("ipAddress")
    private String ipAddress;
    
    @JsonProperty("deviceFingerprint")
    private String deviceFingerprint;
}
