package tn.freelancy.skillmanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.freelancy.skillmanagement.entity.Education;
import tn.freelancy.skillmanagement.service.EducationService;

import java.util.List;

@RestController
@RequestMapping("/api/education")
public class EducationController {

    @Autowired
    private EducationService educationService;

    @PostMapping("/user/{userId}")
    public Education createEducation(
            @PathVariable Long userId,
            @RequestBody Education education) {

        return educationService.createEducation(userId, education);
    }


    @GetMapping
    public List<Education> getAll() {
        return educationService.getAllEducations();
    }

    @GetMapping("/{id}")
    public Education getById(@PathVariable Long id) {
        return educationService.getEducationById(id);
    }

    @PutMapping
    public Education update(@RequestBody Education education) {
        return educationService.updateEducation(education);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        educationService.deleteEducation(id);
    }
    @GetMapping("/user/{userId}/latest")
    public Education getLatestEducation(@PathVariable Long userId) {

        return educationService.getLatestEducation(userId);
    }
}
