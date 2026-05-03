package tn.esprit.challengeservice.dtos;

import lombok.Data;
import tn.esprit.challengeservice.entities.TaskStatus;

import java.util.Date;

@Data
public class TaskDTO {
    private String title;
    private String description;
    private TaskStatus status;
    private Date submittedAt;
    private Date deadline;
    private Long progress;
}