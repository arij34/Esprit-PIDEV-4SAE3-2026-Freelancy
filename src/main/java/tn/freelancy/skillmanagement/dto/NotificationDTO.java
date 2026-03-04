package tn.freelancy.skillmanagement.dto;

import tn.freelancy.skillmanagement.entity.Notification;
import tn.freelancy.skillmanagement.entity.NotificationType;

import java.time.LocalDateTime;

public class NotificationDTO {

    private Long id;
    private NotificationType type;
    private String message;
    private String recipient;
    private String skillName;
    private Long freelancerId;
    private String freelancerName;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationDTO() {}

    // Convertir entité → DTO
    public static NotificationDTO from(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.id             = n.getId();
        dto.type           = n.getType();
        dto.message        = n.getMessage();
        dto.recipient      = n.getRecipient();
        dto.skillName      = n.getSkillName();
        dto.freelancerId   = n.getFreelancerId();
        dto.freelancerName = n.getFreelancerName();
        dto.read           = n.isRead();
        dto.createdAt      = n.getCreatedAt();
        return dto;
    }

    // ── Getters ────────────────────────────────────────────────────
    public Long getId()                  { return id; }
    public NotificationType getType()    { return type; }
    public String getMessage()           { return message; }
    public String getRecipient()         { return recipient; }
    public String getSkillName()         { return skillName; }
    public Long getFreelancerId()        { return freelancerId; }
    public String getFreelancerName()    { return freelancerName; }
    public boolean isRead()              { return read; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // ── Setters ────────────────────────────────────────────────────
    public void setId(Long id)                      { this.id = id; }
    public void setType(NotificationType type)       { this.type = type; }
    public void setMessage(String message)           { this.message = message; }
    public void setRecipient(String recipient)       { this.recipient = recipient; }
    public void setSkillName(String skillName)       { this.skillName = skillName; }
    public void setFreelancerId(Long freelancerId)   { this.freelancerId = freelancerId; }
    public void setFreelancerName(String name)       { this.freelancerName = name; }
    public void setRead(boolean read)                { this.read = read; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}