package tn.esprit.matching.dto;

import tn.esprit.matching.clients.AvailabilityDTO;
import tn.esprit.matching.clients.FreelancerSkillMatchingResponse;

import java.util.List;

public class FreelancerMatchDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private String role;
    private String availability;
    private List<String> skills;
    private double matchScore; // ✅ double au lieu de int

    private double rating = 0.0;
    private int reviewCount = 0;
    private int activeProjects = 0;
    private int completedProjects = 0;

    // ✅ constructeur complet
    public FreelancerMatchDTO(Long id, String firstName, String lastName,
                              String title, String role, String availability,
                              List<String> skills, double matchScore) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.title = title;
        this.role = role;
        this.availability = availability;
        this.skills = skills;
        this.matchScore = matchScore;
    }

    // ✅ constructeur simple (fallback)
    public FreelancerMatchDTO(Long id, double matchScore) {
        this.id = id;
        this.matchScore = matchScore;
    }

    public FreelancerMatchDTO(Long userId,
                              AvailabilityDTO availability,
                              List<FreelancerSkillMatchingResponse> skills,
                              double finalScore) {
        this.id = userId;
        this.availability = availability != null ? availability.getStatus() : "UNKNOWN";
        this.skills = skills != null
                ? skills.stream().map(FreelancerSkillMatchingResponse::getSkillName).toList()
                : List.of();
        this.matchScore = finalScore;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public int getActiveProjects() { return activeProjects; }
    public int getCompletedProjects() { return completedProjects; }

    public void setRating(double rating) { this.rating = rating; }

    public void setActiveProjects(int active) {
        this.activeProjects = active;
    }

    public void setCompletedProjects(int i) {
        this.completedProjects = i;
    }

}