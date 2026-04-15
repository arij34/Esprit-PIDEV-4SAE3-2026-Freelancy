// package à adapter : tn.esprit.matching.dto
package tn.esprit.matching.dto;

public class AdminMatchingStatsDTO {

    // Invitations
    private long totalInvitations;
    private long pendingInvitations;
    private long acceptedInvitations;
    private long declinedInvitations;
    private long trashInvitations;

    // Matchings
    private long totalMatchings;
    private double avgFinalScore;     // moyenne scoreFinal
    private double maxFinalScore;     // max scoreFinal
    private double minFinalScore;     // min scoreFinal

    public AdminMatchingStatsDTO() {}

    // getters & setters

    public long getTotalInvitations() { return totalInvitations; }
    public void setTotalInvitations(long totalInvitations) { this.totalInvitations = totalInvitations; }

    public long getPendingInvitations() { return pendingInvitations; }
    public void setPendingInvitations(long pendingInvitations) { this.pendingInvitations = pendingInvitations; }

    public long getAcceptedInvitations() { return acceptedInvitations; }
    public void setAcceptedInvitations(long acceptedInvitations) { this.acceptedInvitations = acceptedInvitations; }

    public long getDeclinedInvitations() { return declinedInvitations; }
    public void setDeclinedInvitations(long declinedInvitations) { this.declinedInvitations = declinedInvitations; }

    public long getTrashInvitations() { return trashInvitations; }
    public void setTrashInvitations(long trashInvitations) { this.trashInvitations = trashInvitations; }

    public long getTotalMatchings() { return totalMatchings; }
    public void setTotalMatchings(long totalMatchings) { this.totalMatchings = totalMatchings; }

    public double getAvgFinalScore() { return avgFinalScore; }
    public void setAvgFinalScore(double avgFinalScore) { this.avgFinalScore = avgFinalScore; }

    public double getMaxFinalScore() { return maxFinalScore; }
    public void setMaxFinalScore(double maxFinalScore) { this.maxFinalScore = maxFinalScore; }

    public double getMinFinalScore() { return minFinalScore; }
    public void setMinFinalScore(double minFinalScore) { this.minFinalScore = minFinalScore; }
}