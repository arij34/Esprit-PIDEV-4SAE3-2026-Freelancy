package tn.esprit.matching.service;// package tn.esprit.matching.service;

import org.springframework.stereotype.Service;
import tn.esprit.matching.dto.AdminMatchingStatsDTO;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.entity.Matching;
import tn.esprit.matching.repository.InvitationRepository;
import tn.esprit.matching.repository.MatchingRepository;

import java.util.DoubleSummaryStatistics;
import java.util.List;

@Service
public class AdminMatchingStatsService {

    private final InvitationRepository invitationRepository;
    private final MatchingRepository matchingRepository;

    public AdminMatchingStatsService(InvitationRepository invitationRepository,
                                     MatchingRepository matchingRepository) {
        this.invitationRepository = invitationRepository;
        this.matchingRepository = matchingRepository;
    }

    public AdminMatchingStatsDTO getGlobalStats() {
        AdminMatchingStatsDTO dto = new AdminMatchingStatsDTO();

        // ===== Invitations =====
        List<Invitation> invitations = invitationRepository.findAll();
        dto.setTotalInvitations(invitations.size());

        long pending  = invitations.stream().filter(i -> i.getStatus().name().equals("PENDING")).count();
        long accepted = invitations.stream().filter(i -> i.getStatus().name().equals("ACCEPTED")).count();
        long declined = invitations.stream().filter(i -> i.getStatus().name().equals("DECLINED")).count();
        long trash    = invitations.stream().filter(i -> i.getStatus().name().equals("TRASH")).count();

        dto.setPendingInvitations(pending);
        dto.setAcceptedInvitations(accepted);
        dto.setDeclinedInvitations(declined);
        dto.setTrashInvitations(trash);

        // ===== Matchings =====
        List<Matching> matchings = matchingRepository.findAll();
        dto.setTotalMatchings(matchings.size());

        if (!matchings.isEmpty()) {
            DoubleSummaryStatistics stats = matchings.stream()
                    .mapToDouble(Matching::getScoreFinal)
                    .summaryStatistics();

            dto.setAvgFinalScore(stats.getAverage());
            dto.setMaxFinalScore(stats.getMax());
            dto.setMinFinalScore(stats.getMin());
        } else {
            dto.setAvgFinalScore(0.0);
            dto.setMaxFinalScore(0.0);
            dto.setMinFinalScore(0.0);
        }

        return dto;
    }
}