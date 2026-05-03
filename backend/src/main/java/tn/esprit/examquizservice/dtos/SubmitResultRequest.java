package tn.esprit.examquizservice.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitResultRequest {

    @JsonProperty("attemptId")
    private Long attemptId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("examId")
    private Long examId;

    @JsonProperty("earnedPoints")
    private Double earnedPoints;

    @JsonProperty("totalPoints")
    private Double totalPoints;

    @JsonProperty("timeTakenSeconds")
    private Integer timeTakenSeconds;

    /** Set to true when the exam was auto-submitted due to proctoring violations or timeout */
    @JsonProperty("autoSubmitted")
    private Boolean autoSubmitted;

    /** Optional: key = questionId, value = chosen answer text */
    @JsonProperty("answers")
    private Map<Long, String> answers;
}
