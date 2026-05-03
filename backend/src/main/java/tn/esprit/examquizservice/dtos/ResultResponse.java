package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import tn.esprit.examquizservice.entities.ResultStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("examId")
    private Long examId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("attemptId")
    private Long attemptId;

    @JsonProperty("scorePercent")
    private Double scorePercent;

    @JsonProperty("earnedPoints")
    private Double earnedPoints;

    @JsonProperty("totalPoints")
    private Double totalPoints;

    @JsonProperty("status")
    private ResultStatus status;

    @JsonProperty("submittedAt")
    private LocalDateTime submittedAt;

    @JsonProperty("timeTakenSeconds")
    private Integer timeTakenSeconds;

    @JsonProperty("answersJson")
    private String answersJson;
}
