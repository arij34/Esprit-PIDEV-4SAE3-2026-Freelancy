package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.entities.QuizSetting;
import tn.esprit.examquizservice.services.QuizSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-settings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QuizSettingController {
    private final QuizSettingService service;

    @PostMapping
    public QuizSetting create(@RequestBody QuizSetting value) {
        return service.create(value);
    }

    @GetMapping
    public List<QuizSetting> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public QuizSetting getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public QuizSetting update(@PathVariable Long id, @RequestBody QuizSetting value) {
        return service.update(id, value);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
