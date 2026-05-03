package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import tn.esprit.examquizservice.entities.ExamViolationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViolationDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("examId")
    private Long examId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("type")
    private ExamViolationType type;

    @JsonProperty("severity")
    private tn.esprit.examquizservice.entities.ViolationSeverity severity;

    @JsonProperty("attemptId")
    private Long attemptId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("details")
    private String details;

    @JsonProperty("actionTaken")
    private String actionTaken;

    @JsonProperty("countSnapshot")
    private Integer countSnapshot;

}
