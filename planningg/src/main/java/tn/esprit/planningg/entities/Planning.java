package tn.esprit.planningg.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Planning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private LocalDateTime dateCreation;
    @JsonIgnore

    @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL)
    private List<Task> tasks;

    public Planning() {
    }

    public Planning(Long id, String type, LocalDateTime dateCreation, List<Task> tasks) {
        this.id = id;
        this.type = type;
        this.dateCreation = dateCreation;
        this.tasks = tasks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
