package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.entities.Attempt;
import tn.esprit.examquizservice.services.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AttemptController {
    private final AttemptService service;
    @PostMapping public Attempt create(@RequestBody Attempt value) { return service.create(value); }
    @GetMapping public List<Attempt> getAll() { return service.findAll(); }
    @GetMapping("/{id}") public Attempt getById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}") public Attempt update(@PathVariable Long id, @RequestBody Attempt value) { return service.update(id, value); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
