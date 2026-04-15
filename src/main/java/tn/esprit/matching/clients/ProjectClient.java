package tn.esprit.matching.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "projet-module")
public interface ProjectClient {

    @GetMapping("/projects/{id}")
    ProjectDTO getProjectById(@PathVariable Long id);

    @GetMapping("/projects")
    List<ProjectDTO> getAllProjects();

    @GetMapping("/projects/{id}/skills")
    List<ProjectSkillDTO> getProjectSkills(@PathVariable Long id);

    @GetMapping("/analysis/{id}/analysis")
    ProjectAnalysisDTO getProjectAnalysis(@PathVariable Long id);

    @GetMapping("/proposals/matching/freelancer/{freelancerId}")
    List<ProposalDTO> getProposalsByFreelancer(
            @PathVariable("freelancerId") Long freelancerId
    );

    @GetMapping("/projects/{projectId}/invitation-data")
    InvitationProjectDTO getInvitationData(@PathVariable("projectId") Long projectId);

    @PutMapping("/proposals/matching/{projectId}/{freelancerId}/accept")
    void acceptProposal(
            @PathVariable("projectId") Long projectId,
            @PathVariable("freelancerId") Long freelancerId
    );

    @PostMapping("/proposals/matching")
    Map<String, Object> createProposalFromMatching(
            @RequestBody Map<String, Object> body
    );

    // récupérer id + status d'une proposal par (projectId, freelancerId)
    @GetMapping("/proposals/by-project-and-freelancer")
    Map<String, Object> getProposalByProjectAndFreelancer(
            @RequestParam("projectId") Long projectId,
            @RequestParam("freelancerId") Long freelancerId
    );

    // mettre à jour le statut (et déclencher l'email)
    @PutMapping("/proposals/{id}/status")
    Map<String, Object> updateProposalStatus(
            @PathVariable("id") Long proposalId,
            @RequestBody Map<String, String> body
    );
}