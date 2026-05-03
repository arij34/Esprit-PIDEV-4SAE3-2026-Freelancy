package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.entities.Question;
import tn.esprit.examquizservice.services.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService service;
    @PostMapping public Question create(@RequestBody Question value) { return service.create(value); }
    @GetMapping public List<Question> getAll() { return service.findAll(); }
    @GetMapping("/by-exam/{examId}") public List<Question> getByExamId(@PathVariable Long examId) { return service.findByExamId(examId); }
    @GetMapping("/{id}") public Question getById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}") public Question update(@PathVariable Long id, @RequestBody Question value) { return service.update(id, value); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
