package tn.esprit.contrat.dto;

import java.util.List;

public class AiContractResult {

    private String             generatedDescription;
    private List<String>       clauses;
    private List<MilestoneRequest> suggestedMilestones;

    public AiContractResult() {}

    public AiContractResult(String generatedDescription,
                            List<String> clauses,
                            List<MilestoneRequest> suggestedMilestones) {
        this.generatedDescription = generatedDescription;
        this.clauses              = clauses;
        this.suggestedMilestones  = suggestedMilestones;
    }

    public String getGeneratedDescription()              { return generatedDescription; }
    public void setGeneratedDescription(String d)        { this.generatedDescription = d; }

    public List<String> getClauses()                     { return clauses; }
    public void setClauses(List<String> clauses)         { this.clauses = clauses; }

    public List<MilestoneRequest> getSuggestedMilestones()           { return suggestedMilestones; }
    public void setSuggestedMilestones(List<MilestoneRequest> milestones) { this.suggestedMilestones = milestones; }
}