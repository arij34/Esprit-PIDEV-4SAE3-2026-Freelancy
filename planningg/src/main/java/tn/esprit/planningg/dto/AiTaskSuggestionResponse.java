package tn.esprit.planningg.dto;

import java.util.List;

public class AiTaskSuggestionResponse {
    private String model;
    private List<AiTaskSuggestionItem> suggestions;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<AiTaskSuggestionItem> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<AiTaskSuggestionItem> suggestions) {
        this.suggestions = suggestions;
    }
}
