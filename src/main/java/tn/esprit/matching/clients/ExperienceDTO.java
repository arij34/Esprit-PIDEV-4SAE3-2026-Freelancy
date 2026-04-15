package tn.esprit.matching.clients;

public class ExperienceDTO {

    private Long id;
    private String title;
    private String description;
    private Long userId;

    public ExperienceDTO() {}

    public ExperienceDTO(Long id, String title, String description, Long userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Long getUserId() { return userId; }
}