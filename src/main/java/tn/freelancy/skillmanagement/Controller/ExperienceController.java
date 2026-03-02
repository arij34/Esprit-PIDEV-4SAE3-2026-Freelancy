package tn.freelancy.skillmanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.freelancy.skillmanagement.entity.Experience;
import tn.freelancy.skillmanagement.service.ExperienceService;

import java.util.List;

@RestController
@RequestMapping("/api/experience")
public class ExperienceController {

    @Autowired
    private ExperienceService experienceService;

    @PostMapping("/user/{userId}")
    public Experience create(@PathVariable Long userId,@RequestBody Experience experience) {
        return experienceService.createExperience(userId,experience);
    }

    @GetMapping
    public List<Experience> getAll() {
        return experienceService.getAllExperiences();
    }

    @GetMapping("/{id}")
    public Experience getById(@PathVariable Long id) {
        return experienceService.getExperienceById(id);
    }

    @PutMapping
    public Experience update(@RequestBody Experience experience) {
        return experienceService.updateExperience(experience);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        experienceService.deleteExperience(id);
    }
    @GetMapping("/user/{userId}/total-years")
    public long getTotalYears(@PathVariable Long userId) {
        return (long) experienceService.calculateTotalYearsByUser(userId);
    }
}
