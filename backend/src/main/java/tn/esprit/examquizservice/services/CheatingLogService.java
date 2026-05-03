package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.CheatingLog;
import tn.esprit.examquizservice.repositories.CheatingLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheatingLogService {
    private final CheatingLogRepository repository;
    public CheatingLog create(CheatingLog o) { return repository.save(o); }
    public List<CheatingLog> findAll() { return repository.findAll(); }
    public CheatingLog findById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Cheating log not found with id: " + id)); }
    public CheatingLog update(Long id, CheatingLog o) { o.setId(findById(id).getId()); return repository.save(o); }
    public void delete(Long id) { repository.deleteById(id); }
}
