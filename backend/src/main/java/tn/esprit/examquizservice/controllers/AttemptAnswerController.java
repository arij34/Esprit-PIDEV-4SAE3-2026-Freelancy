package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.entities.AttemptAnswer;
import tn.esprit.examquizservice.services.AttemptAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempt-answers")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AttemptAnswerController {
    private final AttemptAnswerService service;
    @PostMapping public AttemptAnswer create(@RequestBody AttemptAnswer value) { return service.create(value); }
    @GetMapping public List<AttemptAnswer> getAll() { return service.findAll(); }
    @GetMapping("/{id}") public AttemptAnswer getById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}") public AttemptAnswer update(@PathVariable Long id, @RequestBody AttemptAnswer value) { return service.update(id, value); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
