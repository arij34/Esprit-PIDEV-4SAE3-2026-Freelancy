package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.entities.ExamSetting;
import tn.esprit.examquizservice.services.ExamSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam-settings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ExamSettingController {
    private final ExamSettingService service;
    @PostMapping public ExamSetting create(@RequestBody ExamSetting value) { return service.create(value); }
    @GetMapping public List<ExamSetting> getAll() { return service.findAll(); }
    @GetMapping("/{id}") public ExamSetting getById(@PathVariable Long id) { return service.findById(id); }
    @GetMapping("/by-exam/{examId}") public ExamSetting getByExamId(@PathVariable Long examId) { return service.findByExamId(examId); }
    @PutMapping("/{id}") public ExamSetting update(@PathVariable Long id, @RequestBody ExamSetting value) { return service.update(id, value); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
