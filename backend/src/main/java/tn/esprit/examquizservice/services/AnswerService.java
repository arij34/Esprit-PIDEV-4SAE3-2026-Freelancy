package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import tn.esprit.examquizservice.repositories.AnswerRepository;
@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository repository;
    public Answer create(Answer o) { return repository.save(o); }
    public List<Answer> findAll() { return repository.findAll(); }
    public List<Answer> findByQuestionId(Long questionId) { return repository.findByQuestionId(questionId); }
    public Answer findById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Answer not found with id: " + id)); }
    public Answer update(Long id, Answer o) { o.setId(findById(id).getId()); return repository.save(o); }
    public void delete(Long id) { repository.deleteById(id); }
}
