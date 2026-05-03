package tn.esprit.examquizservice.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum QuestionType {
    MCQ,
    TRUE_FALSE,
    SHORT;

    @JsonCreator
    public static QuestionType fromValue(String value) {
        if (value == null) return null;
        return switch (value.trim().toUpperCase()) {
            case "MCQ" -> MCQ;
            case "TRUE_FALSE", "TRUE/FALSE", "TRUEFALSE" -> TRUE_FALSE;
            case "SHORT" -> SHORT;
            default -> throw new IllegalArgumentException("Unknown question type: " + value);
        };
    }
}
