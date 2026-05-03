package tn.esprit.examquizservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLiveExamSnapshotDTO {

    private Long examId;
    private String examTitle;
    /** Candidates whose status is IN_PROGRESS right now */
    private Integer activeCandidates;
    /** All candidates visible in this snapshot (IN_PROGRESS + AUTO_SUBMITTED + SUBMITTED) */
    private Integer totalParticipants;
    private LocalDateTime generatedAt;
    private List<AdminLiveCandidateDTO> candidates;
    private List<AdminLiveEventDTO> recentEvents;
}