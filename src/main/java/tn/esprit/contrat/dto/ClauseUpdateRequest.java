package tn.esprit.contrat.dto;

public class ClauseUpdateRequest {
    private String text;

    public ClauseUpdateRequest() {}

    public ClauseUpdateRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

