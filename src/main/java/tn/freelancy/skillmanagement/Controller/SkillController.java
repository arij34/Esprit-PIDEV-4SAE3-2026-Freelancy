package tn.freelancy.skillmanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.freelancy.skillmanagement.dto.SkillMatchResult;
import tn.freelancy.skillmanagement.entity.Skill;
import tn.freelancy.skillmanagement.service.SkillMatcherService;
import tn.freelancy.skillmanagement.service.SkillService;

import java.util.List;

@RestController
@RequestMapping("/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @Autowired
    private SkillMatcherService skillMatcherService;

    @PostMapping
    public Skill create(@RequestBody Skill skill) {
        return skillService.createSkill(skill);
    }

    @GetMapping
    public List<Skill> getAll() {
        return skillService.getAllSkills();
    }

    @GetMapping("/{id}")
    public Skill getById(@PathVariable Long id) {
        return skillService.getSkillById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Skill> update(@PathVariable Long id, @RequestBody Skill skill) {

        Skill existing = skillService.getSkillById(id);

        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        existing.setName(skill.getName());
        existing.setNormalizedName(skill.getNormalizedName());
        existing.setCategory(skill.getCategory());

        return ResponseEntity.ok(skillService.updateSkill(existing));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        skillService.deleteSkill(id);
    }
    @GetMapping("/match")
    public ResponseEntity<SkillMatchResult> matchSkill(@RequestParam String input) {

        if (input == null || input.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SkillMatchResult result = skillMatcherService.findMatchOrSuggest(input.trim());

        if (result == null) {
            return ResponseEntity.ok().build(); 
        }

        return ResponseEntity.ok(result);
    }
}