package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.QuizSetting;
import tn.esprit.examquizservice.repositories.QuizSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizSettingService {
    private final QuizSettingRepository repository;

    public QuizSetting create(QuizSetting value) {
        return repository.save(value);
    }

    public List<QuizSetting> findAll() {
        return repository.findAll();
    }

    public QuizSetting findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz setting not found with id: " + id));
    }

    public QuizSetting update(Long id, QuizSetting value) {
        value.setId(findById(id).getId());
        return repository.save(value);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
