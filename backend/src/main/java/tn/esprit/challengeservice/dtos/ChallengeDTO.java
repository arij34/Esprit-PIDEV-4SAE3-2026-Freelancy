package tn.esprit.challengeservice.dtos;

import lombok.Data;
import tn.esprit.challengeservice.entities.ChallengeDifficulty;
import tn.esprit.challengeservice.entities.ChallengeStatus;

import java.util.Date;

@Data
public class ChallengeDTO {
    private String title;
    private String description;
    private String category;
    private String technology;
    private String githubUrl;
    private Date startDate;
    private Date endDate;
    private String image;
    private Long points;
    private ChallengeDifficulty difficulty;
    private ChallengeStatus status;
    private Integer maxParticipants;
}