package tn.freelancy.skillmanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.freelancy.skillmanagement.clients.UserDto;
import tn.freelancy.skillmanagement.clients.UserServiceClient;
import tn.freelancy.skillmanagement.dto.DuplicateSkillDTO;
import tn.freelancy.skillmanagement.dto.FreelancerSkillMatchingResponse;
import tn.freelancy.skillmanagement.dto.SkillCheckResponse;
import tn.freelancy.skillmanagement.dto.SkillMatchResult;
import tn.freelancy.skillmanagement.entity.FreelancerSkill;
import tn.freelancy.skillmanagement.entity.level;
import tn.freelancy.skillmanagement.service.FreelancerSkillService;
import tn.freelancy.skillmanagement.service.SkillMatcherService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/freelancer-skill")
public class FreelancerSkillController {

    @Autowired
    private FreelancerSkillService freelancerSkillService;

    @Autowired
    private SkillMatcherService skillMatcherService;

    @Autowired
    private UserServiceClient userServiceClient;

    // ✅ CREATE (avec gestion suggestion)
    @PostMapping("/user/me")
    public ResponseEntity<?> createFreelancerSkillForCurrentUser(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String skillInput,
            @RequestBody FreelancerSkill freelancerSkill) {

        try {
            UserDto currentUser = userServiceClient.getCurrentUser(authorization);
            Long userId = currentUser.getId();

            SkillMatchResult match = skillMatcherService.findMatchOrSuggest(skillInput.trim());

            // 🔴 DID YOU MEAN
            if (match != null && match.isSuggestion()) {
                return ResponseEntity.ok(Map.of(
                        "type", "suggestion",
                        "message", "Did you mean?",
                        "suggestedSkill", match.getSkill().getName(),
                        "confidence", match.getConfidence()
                ));
            }

            FreelancerSkill saved = freelancerSkillService
                    .createFreelancerSkill(userId, freelancerSkill, skillInput);

            return ResponseEntity.ok(Map.of(
                    "type", "success",
                    "data", buildSkillResponse(saved)
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ CREATE CV
    @PostMapping("/CV/me")
    public ResponseEntity<?> createFreelancerSkillCVForCurrentUser(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String skillInput,
            @RequestBody FreelancerSkill freelancerSkill) {

        try {
            UserDto currentUser = userServiceClient.getCurrentUser(authorization);
            Long userId = currentUser.getId();

            FreelancerSkill saved = freelancerSkillService
                    .createFreelancerSkillCv(userId, freelancerSkill, skillInput);

            return ResponseEntity.ok(Map.of(
                    "type", "success",
                    "data", buildSkillResponse(saved)
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ GET skills user connecté
    @GetMapping("/user/me")
    public ResponseEntity<?> getAllForCurrentUser(
            @RequestHeader("Authorization") String authorization) {

        try {
            UserDto currentUser = userServiceClient.getCurrentUser(authorization);
            Long userId = currentUser.getId();

            List<FreelancerSkill> skills =
                    freelancerSkillService.getFreelancerSkillsByUserId(userId);

            return ResponseEntity.ok(skills);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        freelancerSkillService.deleteFreelancerSkill(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ LEVEL
    @GetMapping("/level/{years}")
    public ResponseEntity<Map<String, Object>> getLevelByYears(@PathVariable int years) {

        level calculatedLevel = freelancerSkillService.calculateLevel(years);

        Map<String, Object> response = new HashMap<>();
        response.put("years", years);
        response.put("level", calculatedLevel.ordinal() + 1);
        response.put("label", calculatedLevel.name());

        return ResponseEntity.ok(response);
    }

    // ✅ DUPLICATES (corrigé)
    @GetMapping("/user/{userId}/duplicates")
    public ResponseEntity<List<DuplicateSkillDTO>> getDuplicateSkills(@PathVariable Long userId) {
        return ResponseEntity.ok(freelancerSkillService.detectDuplicates(userId));
    }

    // ✅ CHECK SKILLS (unique API)
    @PostMapping("/check-skills/me")
    public ResponseEntity<SkillCheckResponse> checkSkillsForCurrentUser(
            @RequestHeader("Authorization") String authorization,
            @RequestBody List<String> skills) {

        UserDto currentUser = userServiceClient.getCurrentUser(authorization);
        Long userId = currentUser.getId();

        SkillCheckResponse response =
                freelancerSkillService.checkExistingSkills(userId, skills);

        return ResponseEntity.ok(response);
    }

    // ===== HELPER =====
    private Map<String, Object> buildSkillResponse(FreelancerSkill saved) {

        Map<String, Object> response = new HashMap<>();

        response.put("id", saved.getId());
        response.put("level", saved.getLevel());
        response.put("yearsExperience", saved.getYearsExperience());

        response.put("skillName",
                saved.getSkill() != null
                        ? saved.getSkill().getName()
                        : saved.getCustomSkillName()
        );

        response.put("isCustom", saved.getSkill() == null);

        return response;
    }


    @GetMapping("/user/{userId}/matching")
    public List<FreelancerSkillMatchingResponse> getSkillsForMatching(@PathVariable Long userId) {
        return freelancerSkillService.getSkillsForMatching(userId);
    }
}