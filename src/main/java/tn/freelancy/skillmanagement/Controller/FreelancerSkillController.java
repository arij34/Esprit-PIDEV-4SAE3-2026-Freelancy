package tn.freelancy.skillmanagement.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.freelancy.skillmanagement.dto.SkillMatchResult;
import tn.freelancy.skillmanagement.entity.FreelancerSkill;
import tn.freelancy.skillmanagement.entity.Skill;
import tn.freelancy.skillmanagement.entity.level;
import tn.freelancy.skillmanagement.service.FreelancerSkillService;
import tn.freelancy.skillmanagement.service.SkillMatcherService;
import tn.freelancy.skillmanagement.service.SkillService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/freelancer-skill")
public class FreelancerSkillController {

    @Autowired
    private FreelancerSkillService freelancerSkillService;

    // ✅ CREATE
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createFreelancerSkill(
            @PathVariable Long userId,
            @RequestParam String skillInput,
            @RequestBody FreelancerSkill freelancerSkill) {

        try {
            FreelancerSkill saved = freelancerSkillService
                    .createFreelancerSkill(userId, freelancerSkill, skillInput);

            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("level", saved.getLevel());
            response.put("yearsExperience", saved.getYearsExperience());

            if (saved.getSkill() != null) {
                response.put("skillName", saved.getSkill().getName());
            } else {
                response.put("skillName", saved.getCustomSkillName());
            }

            return ResponseEntity.ok(response);

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

            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("level", saved.getLevel());
            response.put("yearsExperience", saved.getYearsExperience());

            if (saved.getSkill() != null) {
                response.put("skillName", saved.getSkill().getName());
            } else {
                response.put("skillName", saved.getCustomSkillName());
            }

            return ResponseEntity.ok(response);

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
}