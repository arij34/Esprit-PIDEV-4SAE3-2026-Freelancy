package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProctoringViolationRequest {
    @JsonProperty("examId")
    private Long examId;

    @JsonProperty("attemptId")
    private Long attemptId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("message")
    private String message;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("timestamp")
    private String timestamp;
}
