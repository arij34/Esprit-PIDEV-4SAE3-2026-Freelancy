package tn.esprit.examquizservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLiveEventDTO {

    private String source;
    private Long examId;
    private Long attemptId;
    private Long userId;
    private String type;
    private String severity;
    private String action;
    private String details;
    private LocalDateTime timestamp;
}