package tn.freelancy.skillmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String role;

    // Associations

    @OneToMany(cascade = CascadeType.ALL, mappedBy="user")
    private List<FreelancerSkill> freelancerSkills;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="user")
    private List<Experience> experiences;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="user")
    private List<Education> educations;


}
