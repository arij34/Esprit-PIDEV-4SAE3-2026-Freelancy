package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cheating_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheatingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private CheatingEventType eventType;
    private LocalDateTime eventTime;
    @Column(columnDefinition = "TEXT")
    private String details;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    @JsonBackReference("attempt-cheating")
    private Attempt attempt;
}
