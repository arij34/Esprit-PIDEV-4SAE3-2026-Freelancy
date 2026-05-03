package tn.esprit.examquizservice.controllers;

import tn.esprit.examquizservice.entities.CheatingLog;
import tn.esprit.examquizservice.services.CheatingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cheating-logs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CheatingLogController {
    private final CheatingLogService service;
    @PostMapping public CheatingLog create(@RequestBody CheatingLog value) { return service.create(value); }
    @GetMapping public List<CheatingLog> getAll() { return service.findAll(); }
    @GetMapping("/{id}") public CheatingLog getById(@PathVariable Long id) { return service.findById(id); }
    @PutMapping("/{id}") public CheatingLog update(@PathVariable Long id, @RequestBody CheatingLog value) { return service.update(id, value); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
