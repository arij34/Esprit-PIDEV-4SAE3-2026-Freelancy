package tn.freelancy.skillmanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.freelancy.skillmanagement.dto.DuplicateSkillDTO;
import tn.freelancy.skillmanagement.dto.SkillCheckResponse;
import tn.freelancy.skillmanagement.dto.SkillMatchResult;
import tn.freelancy.skillmanagement.entity.FreelancerSkill;
import tn.freelancy.skillmanagement.entity.level;
import tn.freelancy.skillmanagement.repository.FreelancerSkillRepository;
import tn.freelancy.skillmanagement.service.FreelancerSkillService;
import tn.freelancy.skillmanagement.service.SkillMatcherService;
import tn.freelancy.skillmanagement.service.SkillService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/freelancer-skill")
public class FreelancerSkillController {

    @Autowired
    private FreelancerSkillService freelancerSkillService;

    @Autowired
    private SkillMatcherService skillMatcherService;

    @Autowired
    private SkillService skillService;

    // ✅ CORRECTION BUG #2 : @Autowired manquant → NullPointerException silencieux
    @Autowired
    private FreelancerSkillRepository freelancerSkillRepository;

    // ✅ CREATE (manuel)
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createFreelancerSkill(
            @PathVariable Long userId,
            @RequestParam String skillInput,
            @RequestBody FreelancerSkill freelancerSkill) {

        try {
            FreelancerSkill saved = freelancerSkillService
                    .createFreelancerSkill(userId, freelancerSkill, skillInput);

            return ResponseEntity.ok(buildSkillResponse(saved));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ CREATE (depuis CV)
    @PostMapping("/CV/{userId}")
    public ResponseEntity<?> createFreelancerSkillCV(
            @PathVariable Long userId,
            @RequestParam String skillInput,
            @RequestBody FreelancerSkill freelancerSkill) {

        try {
            FreelancerSkill saved = freelancerSkillService
                    .createFreelancerSkillCv(userId, freelancerSkill, skillInput);

            return ResponseEntity.ok(buildSkillResponse(saved));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ GET ALL
    @GetMapping
    public List<FreelancerSkill> getAll() {
        return freelancerSkillService.getAllFreelancerSkills();
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public FreelancerSkill getById(@PathVariable Long id) {
        return freelancerSkillService.getFreelancerSkillById(id);
    }

    // ✅ UPDATE
    @PutMapping
    public ResponseEntity<?> updateFreelancerSkill(@RequestBody FreelancerSkill freelancerSkill) {
        try {
            FreelancerSkill saved = freelancerSkillService.updateFreelancerSkill(freelancerSkill);
            return ResponseEntity.ok(buildSkillResponse(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        freelancerSkillService.deleteFreelancerSkill(id);
    }

    // ✅ LEVEL BY YEARS
    @GetMapping("/level/{years}")
    public ResponseEntity<Map<String, Object>> getLevelByYears(@PathVariable int years) {
        level calculatedLevel = freelancerSkillService.calculateLevel(years);

        Map<String, Object> response = new HashMap<>();
        response.put("years", years);
        response.put("level", calculatedLevel.ordinal() + 1);
        response.put("label", calculatedLevel.name());

        return ResponseEntity.ok(response);
    }

    // ✅ DUPLICATE DETECTION
    @GetMapping("/{id}/duplicate-skills")
    public ResponseEntity<List<DuplicateSkillDTO>> getDuplicateSkills(@PathVariable Long id) {
        return ResponseEntity.ok(freelancerSkillService.detectDuplicates(id));
    }

    /**
     * ✅ CHECK SKILLS (utilisé par Angular via checkExistingSkillscv)
     * Délègue entièrement au service pour cohérence et testabilité.
     */
    @PostMapping("/check-skills/{userId}")
    public ResponseEntity<SkillCheckResponse> checkSkills(
            @PathVariable Long userId,
            @RequestBody List<String> skills) {

        SkillCheckResponse response = freelancerSkillService.checkExistingSkills(userId, skills);

        System.out.println("✅ [check-skills] existing: " + response.getExisting());
        System.out.println("✅ [check-skills] newSkills: " + response.getNewSkills());

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ CHECK EXISTING (endpoint alternatif - gardé pour compatibilité)
     * Utilise maintenant le repository correctement injecté.
     */
    @PostMapping("/check-existing/{userId}")
    public ResponseEntity<Map<String, Object>> checkExistingSkills(
            @PathVariable Long userId,
            @RequestBody List<String> skills) {

        List<String> existing = new ArrayList<>();
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

            if (exists) {
                existing.add(skillInput);
            } else {
                newSkills.add(skillInput);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("existing", existing);
        response.put("newSkills", newSkills);

        return ResponseEntity.ok(response);
    }

    // =============== HELPER ===============

    private Map<String, Object> buildSkillResponse(FreelancerSkill saved) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("level", saved.getLevel());
        response.put("yearsExperience", saved.getYearsExperience());
        response.put("skillName", saved.getSkill() != null
                ? saved.getSkill().getName()
                : saved.getCustomSkillName());
        return response;
    }
}