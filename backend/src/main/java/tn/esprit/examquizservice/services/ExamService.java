package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.*;
import tn.esprit.examquizservice.repositories.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;

    public Exam create(Exam exam) {
        if (exam.getCreatedAt() == null) {
            exam.setCreatedAt(LocalDateTime.now());
        }
        syncRelations(exam);
        return examRepository.save(exam);
    }

    public List<Exam> findAll() {
        return examRepository.findAll();
    }

    public Exam findById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + id));
    }

    @Transactional
    public Exam update(Long id, Exam payload) {
        Exam existing = findById(id);

        if (payload.getTitle() != null) existing.setTitle(payload.getTitle());
        if (payload.getDescription() != null) existing.setDescription(payload.getDescription());
        if (payload.getDuration() != null) existing.setDuration(payload.getDuration());
        if (payload.getPoints() != null) existing.setPoints(payload.getPoints());
        if (payload.getPassingScore() != null) existing.setPassingScore(payload.getPassingScore());
        if (payload.getStartDate() != null) existing.setStartDate(payload.getStartDate());
        if (payload.getEndDate() != null) existing.setEndDate(payload.getEndDate());
        if (payload.getMaxAttempts() != null) existing.setMaxAttempts(payload.getMaxAttempts());
        if (payload.getCreatedBy() != null) existing.setCreatedBy(payload.getCreatedBy());
        if (payload.getCreatedAt() != null) existing.setCreatedAt(payload.getCreatedAt());
        if (payload.getStatus() != null) existing.setStatus(payload.getStatus());
        if (payload.getExamType() != null) existing.setExamType(payload.getExamType());

        if (payload.getQuestions() != null) existing.setQuestions(payload.getQuestions());
        if (payload.getExamSetting() != null) existing.setExamSetting(payload.getExamSetting());
        if (payload.getQuizSetting() != null) existing.setQuizSetting(payload.getQuizSetting());
        if (payload.getAttempts() != null) existing.setAttempts(payload.getAttempts());

        syncRelations(existing);
        return examRepository.save(existing);
    }

    public void delete(Long id) {
        if (!examRepository.existsById(id)) {
            throw new RuntimeException("Exam not found with id: " + id);
        }
        examRepository.deleteById(id);
    }

    private void syncRelations(Exam exam) {
        if (exam.getQuestions() != null && Hibernate.isInitialized(exam.getQuestions())) {
            exam.getQuestions().forEach(question -> {
                question.setExam(exam);
                if (question.getAnswers() != null) {
                    question.getAnswers().forEach(answer -> answer.setQuestion(question));
                }
            });
        }
        if (exam.getExamSetting() != null) {
            exam.getExamSetting().setExam(exam);
        }
        if (exam.getQuizSetting() != null) {
            exam.getQuizSetting().setExam(exam);
        }
        if (exam.getAttempts() != null) {
            exam.getAttempts().forEach(attempt -> {
                attempt.setExam(exam);
                if (attempt.getAttemptAnswers() != null) {
                    attempt.getAttemptAnswers().forEach(a -> a.setAttempt(attempt));
                }
                if (attempt.getCheatingLogs() != null) {
                    attempt.getCheatingLogs().forEach(c -> c.setAttempt(attempt));
                }
            });
        }
    }
}
