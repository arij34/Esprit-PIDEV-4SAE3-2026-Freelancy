package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.Question;
import tn.esprit.examquizservice.repositories.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository repository;

    public Question create(Question q) { return repository.save(q); }
    public List<Question> findAll() { return repository.findAll(); }
    public List<Question> findByExamId(Long examId) { return repository.findByExamId(examId); }
    public Question findById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Question not found with id: " + id)); }
    public java.util.Optional<Question> getById(Long id) { return repository.findById(id); }
    public Question update(Long id, Question q) {
        Question existing = findById(id);
        if (q.getQuestionText() != null) existing.setQuestionText(q.getQuestionText());
        if (q.getQuestionType() != null) existing.setQuestionType(q.getQuestionType());
        if (q.getDifficultyLevel() != null) existing.setDifficultyLevel(q.getDifficultyLevel());
        if (q.getPoints() != null) existing.setPoints(q.getPoints());
        if (q.getOrderIndex() != null) existing.setOrderIndex(q.getOrderIndex());
        return repository.save(existing);
    }
    public void delete(Long id) { repository.deleteById(id); }
}
