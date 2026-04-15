package tn.esprit.matching.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "application_questions")
public class ApplicationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // pour l’instant, des questions globales pour toutes les invitations
    // plus tard tu pourras lier par type de projet, etc.
    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "label", nullable = false, columnDefinition = "TEXT")
    private String label;

    @Column(name = "required")
    private boolean required = true;

    // text / number / textarea ... si tu veux évoluer ensuite
    @Column(name = "type")
    private String type = "TEXT";

    public Long getId() { return id; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}