package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ExamStatus {
    DRAFT,
    PUBLISHED,
    COMPLETED,
    CLOSED;

    @JsonCreator
    public static ExamStatus fromValue(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase()) {
            case "DRAFT" -> DRAFT;
            case "PUBLISHED", "ACTIVE" -> PUBLISHED;
            case "CLOSED", "ARCHIVED" -> CLOSED;
            default -> throw new IllegalArgumentException("Unknown exam status: " + value);
        };
    }
}
