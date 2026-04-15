package tn.esprit.matching.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.matching.clients.ProjectClient;
import tn.esprit.matching.clients.ProposalDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class FreelancerStatsService {

    @Autowired
    private ProjectClient projectClient;

    public Map<String, Long> getFreelancerStats(Long freelancerId) {
        LocalDate today = LocalDate.now();

        List<ProposalDTO> proposals = projectClient
                .getProposalsByFreelancer(freelancerId);

        long active = 0;
        long completed = 0;

        for (ProposalDTO proposal : proposals) {
            LocalDate deadline = proposal.getDeadline();

            if (deadline != null && deadline.isBefore(today)) {
                completed++; // deadline dépassée → complété
            } else {
                active++;    // deadline pas encore passée → actif
            }
        }

        return Map.of(
                "activeProjects", active,
                "completedProjects", completed
        );
    }
}