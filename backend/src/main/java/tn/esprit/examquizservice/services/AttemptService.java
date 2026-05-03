package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.Attempt;
import tn.esprit.examquizservice.repositories.AttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttemptService {
    private final AttemptRepository repository;
    public Attempt create(Attempt o) { return repository.save(o); }
    public List<Attempt> findAll() { return repository.findAll(); }
    public Attempt findById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Attempt not found with id: " + id)); }
    public Attempt update(Long id, Attempt o) { o.setId(findById(id).getId()); return repository.save(o); }
    public void delete(Long id) { repository.deleteById(id); }
}
