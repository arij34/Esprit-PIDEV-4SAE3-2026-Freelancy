package tn.esprit.planningg.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    @Enumerated(EnumType.STRING)
    private Priorite priorite;

    @JsonProperty(access = Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(name = "planning_id")
    private Planning planning;

    public Task() {
    }

    public Task(Long id, String titre, String description, LocalDateTime dateDebut, LocalDateTime dateFin, Statut statut, Planning planning) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.planning = planning;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public Priorite getPriorite() {
        return priorite;
    }

    public void setPriorite(Priorite priorite) {
        this.priorite = priorite;
    }

    public Planning getPlanning() {
        return planning;
    }

    public void setPlanning(Planning planning) {
        this.planning = planning;
    }

    @JsonProperty("planningId")
    public Long getPlanningId() {
        return planning != null ? planning.getId() : null;
    }

    @JsonProperty("isOverdue")
    public boolean isOverdue() {
        return dateFin != null
                && statut != Statut.DONE
                && dateFin.isBefore(LocalDateTime.now());
    }
}
