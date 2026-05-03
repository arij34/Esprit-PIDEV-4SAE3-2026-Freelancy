package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import tn.esprit.examquizservice.entities.ExamViolationType;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordViolationRequest {

    @JsonProperty("examId")
    @NotNull(message = "examId cannot be null")
    private Long examId;

    @JsonProperty("userId")
    @NotNull(message = "userId cannot be null")
    private Long userId;

    @JsonProperty("type")
    @NotNull(message = "violation type cannot be null")
    private ExamViolationType type;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("attemptId")
    private Long attemptId;

    @JsonProperty("details")
    private String details;

}