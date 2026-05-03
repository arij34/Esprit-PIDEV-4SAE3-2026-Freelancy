package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ExamType {
    EXAM,
    QUIZ,
    PRACTICE;

    @JsonCreator
    public static ExamType fromValue(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase()) {
            case "EXAM" -> EXAM;
            case "QUIZ" -> QUIZ;
            case "PRACTICE" -> PRACTICE;
            default -> throw new IllegalArgumentException("Unknown exam type: " + value);
        };
    }
}
