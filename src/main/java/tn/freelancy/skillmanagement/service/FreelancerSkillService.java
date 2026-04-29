package tn.freelancy.skillmanagement.service;

import org.springframework.stereotype.Service;
import tn.freelancy.skillmanagement.dto.DuplicateSkillDTO;
import tn.freelancy.skillmanagement.dto.FreelancerSkillMatchingResponse;
import tn.freelancy.skillmanagement.dto.SkillCheckResponse;
import tn.freelancy.skillmanagement.dto.SkillMatchResult;
import tn.freelancy.skillmanagement.entity.*;
import tn.freelancy.skillmanagement.repository.FreelancerSkillRepository;
import tn.freelancy.skillmanagement.repository.PendingSkillRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class FreelancerSkillService {

    private final FreelancerSkillRepository freelancerSkillRepository;
    private final PendingSkillRepository pendingSkillRepository;
    private final SkillMatcherService skillMatcherService;
    private final PendingSkillService pendingSkillService;
    private final SimilarityService similarityService;

    public FreelancerSkillService(FreelancerSkillRepository freelancerSkillRepository,
                                  PendingSkillRepository pendingSkillRepository,
                                  SkillMatcherService skillMatcherService,
                                  PendingSkillService pendingSkillService,
                                  SimilarityService similarityService) {
        this.freelancerSkillRepository = freelancerSkillRepository;
        this.pendingSkillRepository = pendingSkillRepository;
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

    public List<FreelancerSkill> getFreelancerSkillsByUserId(Long userId) {
        return freelancerSkillRepository.findByUserId(userId);
    }

    // ── DELETE avec synchronisation PendingSkill ──────────────────────────────
    /**
     * ✅ SYNC: Si le FreelancerSkill supprimé n'a pas de skill en DB (skill == null)
     *         ET qu'un PendingSkill DRAFT existe avec le même nom → on le supprime aussi.
     *         Si d'autres freelancers utilisent encore ce customSkillName → on garde le pending.
     */
    public void deleteFreelancerSkill(Long id) {
        FreelancerSkill fs = freelancerSkillRepository.findById(id).orElse(null);

        if (fs != null && fs.getSkill() == null && fs.getCustomSkillName() != null) {
            String normalized = fs.getCustomSkillName().toLowerCase().trim();

            // Vérifie si d'autres freelancers utilisent encore ce skill custom
            long count = freelancerSkillRepository
                    .countByCustomSkillNameIgnoreCaseAndIdNot(fs.getCustomSkillName(), id);

            if (count == 0) {
                // Personne d'autre n'utilise ce skill → supprimer le PendingSkill associé
                pendingSkillRepository
                        .findByNormalizedNameAndStatus(normalized, Status.DRAFT)
                        .ifPresent(pendingSkillRepository::delete);
            }
        }

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
    /**
     * ✅ SYNC: PendingSkill créé SEULEMENT si matching == 0 (aucun match trouvé)
     */
    public FreelancerSkill createFreelancerSkill(Long userId,
                                                 FreelancerSkill freelancerSkill,
                                                 String skillInput) {

        freelancerSkill.setUserId(userId);
        freelancerSkill.setLevel(calculateLevel(freelancerSkill.getYearsExperience()));

        skillInput = skillInput.trim();

        SkillMatchResult result = skillMatcherService.findMatchOrSuggest(skillInput);

        if (result != null && result.getSkill() != null) {

            Long skillId = result.getSkill().getIdS();

            boolean exists = freelancerSkillRepository
                    .existsByUserIdAndSkillIdS(userId, skillId);

            if (exists) {
                throw new RuntimeException("Skill already exists");
            }

            // ✅ Match exact ou bon match
            if (!result.isSuggestion()) {
                freelancerSkill.setSkill(result.getSkill());
                freelancerSkill.setCustomSkillName(result.getSkill().getName());
            }

            // ⚠️ CAS "Did you mean"
            else {
                freelancerSkill.setSkill(null);
                freelancerSkill.setCustomSkillName(skillInput);

                // 🔔 Créer PendingSkill
                pendingSkillService.createPendingSkill(
                        skillInput, userId, "User #" + userId, Source.FREELANCER
                );
            }

        } else {
            // ❌ Aucun match
            freelancerSkill.setSkill(null);
            freelancerSkill.setCustomSkillName(skillInput);

            pendingSkillService.createPendingSkill(
                    skillInput, userId, "User #" + userId, Source.FREELANCER
            );
        }

        return freelancerSkillRepository.save(freelancerSkill);
    }

    // ── CREATE (depuis CV) ────────────────────────────────────────────────────

    public FreelancerSkill createFreelancerSkillCv(Long userId,
                                                   FreelancerSkill freelancerSkill,
                                                   String skillInput) {

        freelancerSkill.setUserId(userId);
        freelancerSkill.setLevel(calculateLevel(freelancerSkill.getYearsExperience()));

        skillInput = skillInput.trim();

        SkillMatchResult result = skillMatcherService.findMatchOrSuggest(skillInput);

        if (result != null && result.getSkill() != null) {

            Long skillId = result.getSkill().getIdS();

            boolean exists = freelancerSkillRepository
                    .existsByUserIdAndSkillIdS(userId, skillId);

            if (exists) {
                throw new RuntimeException("Skill already exists for this user");
            }

            if (!result.isSuggestion()) {
                freelancerSkill.setSkill(result.getSkill());
                freelancerSkill.setCustomSkillName(result.getSkill().getName());
            } else {
                freelancerSkill.setSkill(null);
                freelancerSkill.setCustomSkillName(skillInput);

                pendingSkillService.createPendingSkill(
                        skillInput, userId, "User #" + userId, Source.CV
                );
            }

        } else {

            boolean exists = freelancerSkillRepository
                    .existsByUserIdAndCustomSkillNameIgnoreCase(userId, skillInput);

            if (exists) {
                throw new RuntimeException("Skill already exists for this user");
            }

            freelancerSkill.setSkill(null);
            freelancerSkill.setCustomSkillName(skillInput);

            pendingSkillService.createPendingSkill(
                    skillInput, userId, "User #" + userId, Source.CV
            );
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

                SkillMatchResult matchA = skillMatcherService.findMatchOrSuggest(nameA);
                SkillMatchResult matchB = skillMatcherService.findMatchOrSuggest(nameB);

                if (matchA != null && matchB != null) {

                    // ✅ Même skill réel
                    if (matchA.getSkill() != null &&
                            matchB.getSkill() != null &&
                            matchA.getSkill().getIdS().equals(matchB.getSkill().getIdS())) {

                        duplicates.add(new DuplicateSkillDTO(nameA, nameB, 1.0));
                    }

                    // ✅ Similarité forte custom
                    else {
                        double sim = similarityService.calculateSimilarity(
                                nameA.toLowerCase(), nameB.toLowerCase());

                        if (sim > 0.8) {
                            duplicates.add(new DuplicateSkillDTO(nameA, nameB, sim));
                        }
                    }
                }
            }
        }

        return duplicates;
    }
    // ── CHECK EXISTING ────────────────────────────────────────────────────────

    public SkillCheckResponse checkExistingSkills(Long userId, List<String> skills) {

        List<String> existing = new ArrayList<>();
        List<String> newSkills = new ArrayList<>();

        for (String skillInput : skills) {

            skillInput = skillInput.trim();

            SkillMatchResult result = skillMatcherService.findMatchOrSuggest(skillInput);

            boolean exists;

            if (result != null && result.getSkill() != null) {

                exists = freelancerSkillRepository
                        .existsByUserIdAndSkillIdS(userId, result.getSkill().getIdS());

            } else {

                exists = freelancerSkillRepository
                        .existsByUserIdAndCustomSkillNameIgnoreCase(userId, skillInput);
            }

            if (exists) existing.add(skillInput);
            else newSkills.add(skillInput);
        }

        return new SkillCheckResponse(existing, newSkills);
    }

    public List<FreelancerSkillMatchingResponse> getSkillsForMatching(Long userId) {

        List<FreelancerSkill> skills = freelancerSkillRepository.findByUserId(userId);

        return skills.stream().map(skill -> {

            FreelancerSkillMatchingResponse dto = new FreelancerSkillMatchingResponse();

            dto.setUserId(userId);
            dto.setLevel(skill.getLevel());
            dto.setYearsExperience(skill.getYearsExperience());
            dto.setCustomSkillName(skill.getCustomSkillName());

            if (skill.getSkill() != null) {
                dto.setSkillId(skill.getSkill().getIdS());
                dto.setSkillName(skill.getSkill().getName());
                dto.setNormalizedName(skill.getSkill().getNormalizedName());
                dto.setCategory(skill.getSkill().getCategory());
            }

            return dto;

        }).toList();
    }
}