package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.ExamSetting;
import tn.esprit.examquizservice.repositories.ExamSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamSettingService {
    private final ExamSettingRepository repository;
    public ExamSetting create(ExamSetting o) { return repository.save(o); }
    public List<ExamSetting> findAll() { return repository.findAll(); }
    public ExamSetting findById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Exam setting not found with id: " + id)); }
    public ExamSetting findByExamId(Long examId) { return repository.findByExam_Id(examId).orElse(null); }
    public ExamSetting update(Long id, ExamSetting o) { o.setId(findById(id).getId()); return repository.save(o); }
    public void delete(Long id) { repository.deleteById(id); }
}
