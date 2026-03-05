package tn.freelancy.skillmanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.freelancy.skillmanagement.clients.UserDto;
import tn.freelancy.skillmanagement.clients.UserServiceClient;
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
@RequestMapping("/freelancer-skill")
public class FreelancerSkillController {

    @Autowired
    private FreelancerSkillService freelancerSkillService;

    @Autowired
    private SkillMatcherService skillMatcherService;

    @Autowired
    private SkillService skillService;

    @Autowired
    private FreelancerSkillRepository freelancerSkillRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    // ✅ CREATE (manuel)
    @PostMapping("/user/me")
    public ResponseEntity<?> createFreelancerSkillForCurrentUser(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String skillInput,
            @RequestBody FreelancerSkill freelancerSkill) {
        try {
            UserDto currentUser = userServiceClient.getCurrentUser(authorization);
            Long userId = currentUser.getId();
            FreelancerSkill saved = freelancerSkillService
                    .createFreelancerSkill(userId, freelancerSkill, skillInput);
            return ResponseEntity.ok(buildSkillResponse(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ CREATE (depuis CV)
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
            return ResponseEntity.ok(buildSkillResponse(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ AJOUTÉ : GET toutes les skills de l'utilisateur connecté
    @GetMapping("/user/me")
    public ResponseEntity<?> getAllForCurrentUser(
            @RequestHeader("Authorization") String authorization) {
        try {
            UserDto currentUser = userServiceClient.getCurrentUser(authorization);
            Long userId = currentUser.getId();
            List<FreelancerSkill> skills = freelancerSkillService.getFreelancerSkillsByUserId(userId);
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<FreelancerSkill> getAll() {
        return freelancerSkillService.getAllFreelancerSkills();
    }

    @GetMapping("/{id}")
    public FreelancerSkill getById(@PathVariable Long id) {
        return freelancerSkillService.getFreelancerSkillById(id);
    }

    @PutMapping
    public ResponseEntity<?> updateFreelancerSkill(@RequestBody FreelancerSkill freelancerSkill) {
        try {
            FreelancerSkill saved = freelancerSkillService.updateFreelancerSkill(freelancerSkill);
            return ResponseEntity.ok(buildSkillResponse(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        freelancerSkillService.deleteFreelancerSkill(id);
        return ResponseEntity.noContent().build(); // ✅ CORRIGÉ : 204
    }

    @GetMapping("/level/{years}")
    public ResponseEntity<Map<String, Object>> getLevelByYears(@PathVariable int years) {
        level calculatedLevel = freelancerSkillService.calculateLevel(years);
        Map<String, Object> response = new HashMap<>();
        response.put("years", years);
        response.put("level", calculatedLevel.ordinal() + 1);
        response.put("label", calculatedLevel.name());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/duplicate-skills")
    public ResponseEntity<List<DuplicateSkillDTO>> getDuplicateSkills(@PathVariable Long id) {
        return ResponseEntity.ok(freelancerSkillService.detectDuplicates(id));
    }

    @PostMapping("/check-skills/me")
    public ResponseEntity<SkillCheckResponse> checkSkillsForCurrentUser(
            @RequestHeader("Authorization") String authorization,
            @RequestBody List<String> skills) {
        UserDto currentUser = userServiceClient.getCurrentUser(authorization);
        Long userId = currentUser.getId();
        SkillCheckResponse response = freelancerSkillService.checkExistingSkills(userId, skills);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-existing/me")
    public ResponseEntity<Map<String, Object>> checkExistingSkillsForCurrentUser(
            @RequestHeader("Authorization") String authorization,
            @RequestBody List<String> skills) {
        UserDto currentUser = userServiceClient.getCurrentUser(authorization);
        Long userId = currentUser.getId();
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
            if (exists) existing.add(skillInput);
            else newSkills.add(skillInput);
        }

        return ResponseEntity.ok(Map.of("existing", existing, "newSkills", newSkills));
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