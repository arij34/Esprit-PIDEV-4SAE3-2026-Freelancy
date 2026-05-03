package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AttemptStatus {
    IN_PROGRESS,
    SUBMITTED,
    AUTO_SUBMITTED;

    @JsonCreator
    public static AttemptStatus fromValue(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase()) {
            case "IN_PROGRESS", "IN PROGRESS" -> IN_PROGRESS;
            case "SUBMITTED", "COMPLETED" -> SUBMITTED;
            case "AUTO_SUBMITTED", "FLAGGED" -> AUTO_SUBMITTED;
            default -> throw new IllegalArgumentException("Unknown attempt status: " + value);
        };
    }
}
