package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.entities.Answer;
import tn.esprit.examquizservice.services.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService service;
    @PostMapping public Answer create(@RequestBody Answer value) { return service.create(value); }
    @GetMapping public List<Answer> getAll() { return service.findAll(); }
    @GetMapping("/by-question/{questionId}") public List<Answer> getByQuestionId(@PathVariable Long questionId) { return service.findByQuestionId(questionId); }
    @GetMapping("/{id}") public Answer getById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}") public Answer update(@PathVariable Long id, @RequestBody Answer value) { return service.update(id, value); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
