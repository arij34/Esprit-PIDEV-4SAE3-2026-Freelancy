package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.AttemptAnswer;
import tn.esprit.examquizservice.repositories.AttemptAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttemptAnswerService {
    private final AttemptAnswerRepository repository;
    public AttemptAnswer create(AttemptAnswer o) { return repository.save(o); }
    public List<AttemptAnswer> findAll() { return repository.findAll(); }
    public AttemptAnswer findById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Attempt answer not found with id: " + id)); }
    public AttemptAnswer update(Long id, AttemptAnswer o) { o.setId(findById(id).getId()); return repository.save(o); }
    public void delete(Long id) { repository.deleteById(id); }
}
