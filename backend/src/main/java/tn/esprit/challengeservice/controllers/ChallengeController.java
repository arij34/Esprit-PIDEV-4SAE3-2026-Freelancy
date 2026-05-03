package tn.esprit.challengeservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.challengeservice.dtos.ChallengeDTO;
import tn.esprit.challengeservice.entities.Challenge;
import tn.esprit.challengeservice.services.ichallengeService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/challenges")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ChallengeController {

    private final ichallengeService challengeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Challenge addChallenge(@RequestBody ChallengeDTO challengeDto) {
        return challengeService.addChallenge(toChallengeEntity(challengeDto));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('FREELANCER')")
    public List<Challenge> getAllChallenges() {
        return challengeService.getAllChallenges();
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FREELANCER')")
    public Map<String, Long> getActiveChallengesCount() {
        return Map.of("count", challengeService.countActiveChallenges());
    }

    @GetMapping("/count/completed")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FREELANCER')")
    public Map<String, Long> getCompletedChallengesCount() {
        return Map.of("count", challengeService.countCompletedChallenges());
    }

    @GetMapping("/stats/technology-counts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FREELANCER')")
    public Map<String, Long> getTechnologyCounts() {
        return challengeService.getTechnologyCounts();
    }

    @GetMapping("/stats/category-counts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FREELANCER')")
    public Map<String, Long> getCategoryCounts() {
        return challengeService.getCategoryCounts();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('FREELANCER')")
    public Challenge getChallengeById(@PathVariable String id) {
        return challengeService.getChallengeById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Challenge updateChallenge(@PathVariable String id,
                                     @RequestBody ChallengeDTO challengeDto) {
        return challengeService.updateChallenge(id, toChallengeEntity(challengeDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteChallenge(@PathVariable String id) {
        challengeService.deleteChallenge(id);
    }

    private Challenge toChallengeEntity(ChallengeDTO dto) {
        Challenge challenge = new Challenge();
        challenge.setTitle(dto.getTitle());
        challenge.setDescription(dto.getDescription());
        challenge.setCategory(dto.getCategory());
        challenge.setTechnology(dto.getTechnology());
        challenge.setGithubUrl(dto.getGithubUrl());
        challenge.setStartDate(dto.getStartDate());
        challenge.setEndDate(dto.getEndDate());
        challenge.setImage(dto.getImage());
        challenge.setPoints(dto.getPoints());
        challenge.setDifficulty(dto.getDifficulty());
        challenge.setStatus(dto.getStatus());
        challenge.setMaxParticipants(dto.getMaxParticipants());
        return challenge;
    }
}