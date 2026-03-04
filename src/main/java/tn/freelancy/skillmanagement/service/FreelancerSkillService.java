package tn.freelancy.skillmanagement.service;

import org.springframework.stereotype.Service;
import tn.freelancy.skillmanagement.dto.DuplicateSkillDTO;
import tn.freelancy.skillmanagement.dto.SkillCheckResponse;
import tn.freelancy.skillmanagement.dto.SkillMatchResult;
import tn.freelancy.skillmanagement.entity.*;
import tn.freelancy.skillmanagement.repository.FreelancerSkillRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class FreelancerSkillService {

    private final FreelancerSkillRepository freelancerSkillRepository;
    // ✅ SUPPRIMÉ : UserRepository userRepository (n'existe plus)
    private final SkillMatcherService skillMatcherService;
    private final PendingSkillService pendingSkillService;
    private final SimilarityService similarityService;

    public FreelancerSkillService(FreelancerSkillRepository freelancerSkillRepository,
                                  SkillMatcherService skillMatcherService,
                                  PendingSkillService pendingSkillService,
                                  SimilarityService similarityService) {
        this.freelancerSkillRepository = freelancerSkillRepository;
        this.skillMatcherService = skillMatcherService;
        this.pendingSkillService = pendingSkillService;
        this.similarityService = similarityService;
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    public List<FreelancerSkill> getAllFreelancerSkills() {
        return freelancerSkillRepository.findAll();
    }

    public FreelancerSkill getFreelancerSkillById(Long id) {
        return freelancerSkillRepository.findById(id).orElse(null);
    }

    // ✅ AJOUTÉ : skills d'un utilisateur connecté (appelé par GET /user/me)
    public List<FreelancerSkill> getFreelancerSkillsByUserId(Long userId) {
        return freelancerSkillRepository.findByUserId(userId);
    }

    public void deleteFreelancerSkill(Long id) {
        freelancerSkillRepository.deleteById(id);
    }

    // ── LEVEL ─────────────────────────────────────────────────────────────────

    public level calculateLevel(int yearsExperience) {
        if (yearsExperience == 0)      return level.BEGINNER;
        else if (yearsExperience <= 2) return level.ELEMENTARY;
        else if (yearsExperience <= 4) return level.INTERMEDIATE;
        else if (yearsExperience <= 7) return level.ADVANCED;
        else                           return level.EXPERT;
    }

    // ── CREATE (manuel) ───────────────────────────────────────────────────────

    public FreelancerSkill createFreelancerSkill(Long userId,
                                                 FreelancerSkill freelancerSkill,
                                                 String skillInput) {
        // ✅ CORRIGÉ : on stocke directement le userId
        freelancerSkill.setUserId(userId);
        freelancerSkill.setLevel(calculateLevel(freelancerSkill.getYearsExperience()));

        skillInput = skillInput.trim();
        SkillMatchResult result = skillMatcherService.findMatchingSkill(skillInput);

        if (result != null && result.getSkill() != null) {
            freelancerSkill.setSkill(result.getSkill());
            freelancerSkill.setCustomSkillName(skillInput);
        } else {
            // ✅ CORRIGÉ : createPendingSkill prend maintenant userId + nom au lieu de User
            pendingSkillService.createPendingSkill(skillInput, userId, "User #" + userId, Source.FREELANCER);
            freelancerSkill.setSkill(null);
            freelancerSkill.setCustomSkillName(skillInput);
        }

        return freelancerSkillRepository.save(freelancerSkill);
    }

    // ── CREATE (depuis CV) ────────────────────────────────────────────────────

    public FreelancerSkill createFreelancerSkillCv(Long userId,
                                                   FreelancerSkill freelancerSkill,
                                                   String skillInput) {
        // ✅ CORRIGÉ : on stocke directement le userId
        freelancerSkill.setUserId(userId);
        freelancerSkill.setLevel(calculateLevel(freelancerSkill.getYearsExperience()));

        skillInput = skillInput.trim();
        SkillMatchResult result = skillMatcherService.findMatchingSkill(skillInput);

        if (result != null && result.getSkill() != null) {
            Long skillId = result.getSkill().getIdS();
            boolean exists = freelancerSkillRepository.existsByUserIdAndSkillIdS(userId, skillId);
            if (exists) throw new RuntimeException("Skill already exists for this user");

            freelancerSkill.setSkill(result.getSkill());
            freelancerSkill.setCustomSkillName(skillInput);
        } else {
            boolean exists = freelancerSkillRepository
                    .existsByUserIdAndCustomSkillNameIgnoreCase(userId, skillInput);
            if (exists) throw new RuntimeException("Skill already exists for this user");

            // ✅ CORRIGÉ : createPendingSkill prend maintenant userId + nom au lieu de User
            pendingSkillService.createPendingSkill(skillInput, userId, "User #" + userId, Source.FREELANCER);
            freelancerSkill.setSkill(null);
            freelancerSkill.setCustomSkillName(skillInput);
        }

        return freelancerSkillRepository.save(freelancerSkill);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    public FreelancerSkill updateFreelancerSkill(FreelancerSkill freelancerSkill) {
        freelancerSkill.setLevel(calculateLevel(freelancerSkill.getYearsExperience()));
        return freelancerSkillRepository.save(freelancerSkill);
    }

    // ── DUPLICATE DETECTION ───────────────────────────────────────────────────

    public List<DuplicateSkillDTO> detectDuplicates(Long freelancerId) {
        List<FreelancerSkill> skills = freelancerSkillRepository.findByUserId(freelancerId);
        List<DuplicateSkillDTO> duplicates = new ArrayList<>();

        for (int i = 0; i < skills.size(); i++) {
            for (int j = i + 1; j < skills.size(); j++) {
                String nameA = skills.get(i).getCustomSkillName();
                String nameB = skills.get(j).getCustomSkillName();
                if (nameA == null || nameB == null) continue;

                SkillMatchResult matchA = skillMatcherService.findMatchingSkill(nameA);
                SkillMatchResult matchB = skillMatcherService.findMatchingSkill(nameB);

                if (matchA != null && matchB != null
                        && matchA.getSkill() != null
                        && matchB.getSkill() != null
                        && matchA.getSkill().getIdS().equals(matchB.getSkill().getIdS())) {
                    duplicates.add(new DuplicateSkillDTO(nameA, nameB, 1.0));
                }
            }
        }
        return duplicates;
    }

    // ── CHECK EXISTING ────────────────────────────────────────────────────────

    public SkillCheckResponse checkExistingSkills(Long userId, List<String> skills) {
        List<String> existing  = new ArrayList<>();
        List<String> newSkills = new ArrayList<>();

        for (String skillInput : skills) {
            skillInput = skillInput.trim();
            SkillMatchResult result = skillMatcherService.findMatchingSkill(skillInput);

            boolean exists;
            if (result != null && result.getSkill() != null) {
                exists = freelancerSkillRepository
                        .existsByUserIdAndSkillIdS(userId, result.getSkill().getIdS());
            } else {
                exists = freelancerSkillRepository
                        .existsByUserIdAndCustomSkillNameIgnoreCase(userId, skillInput);
            }

            if (exists) existing.add(skillInput);
            else        newSkills.add(skillInput);
        }

        return new SkillCheckResponse(existing, newSkills);
    }
}