package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.entities.Exam;
import tn.esprit.examquizservice.services.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService service;

    @PostMapping
    public Exam create(@RequestBody Exam exam) { return service.create(exam); }

    @GetMapping
    public List<Exam> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public Exam getById(@PathVariable Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public Exam update(@PathVariable Long id, @RequestBody Exam exam) { return service.update(id, exam); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
