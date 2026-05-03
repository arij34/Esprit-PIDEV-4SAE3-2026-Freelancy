package tn.esprit.examquizservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.examquizservice.entities.Answer;
import tn.esprit.examquizservice.entities.Attempt;
import tn.esprit.examquizservice.entities.AttemptAnswer;
import tn.esprit.examquizservice.entities.CheatingLog;
import tn.esprit.examquizservice.entities.Exam;
import tn.esprit.examquizservice.entities.ExamSetting;
import tn.esprit.examquizservice.entities.Question;
import tn.esprit.examquizservice.entities.QuizSetting;
import tn.esprit.examquizservice.repositories.ExamRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @InjectMocks
    private ExamService examService;

    @Test
    void createShouldSetCreatedAtAndSyncRelations() {
        Exam exam = new Exam();
        Question question = new Question();
        Answer answer = new Answer();
        question.setAnswers(List.of(answer));

        ExamSetting examSetting = new ExamSetting();
        QuizSetting quizSetting = new QuizSetting();

        Attempt attempt = new Attempt();
        AttemptAnswer attemptAnswer = new AttemptAnswer();
        CheatingLog cheatingLog = new CheatingLog();
        attempt.setAttemptAnswers(List.of(attemptAnswer));
        attempt.setCheatingLogs(List.of(cheatingLog));

        exam.setQuestions(List.of(question));
        exam.setExamSetting(examSetting);
        exam.setQuizSetting(quizSetting);
        exam.setAttempts(List.of(attempt));

        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Exam saved = examService.create(exam);

        assertNotNull(saved.getCreatedAt());
        assertSame(exam, question.getExam());
        assertSame(question, answer.getQuestion());
        assertSame(exam, examSetting.getExam());
        assertSame(exam, quizSetting.getExam());
        assertSame(exam, attempt.getExam());
        assertSame(attempt, attemptAnswer.getAttempt());
        assertSame(attempt, cheatingLog.getAttempt());
        verify(examRepository).save(exam);
    }

    @Test
    void updateShouldOnlyApplyNonNullFields() {
        Exam existing = new Exam();
        existing.setId(1L);
        existing.setTitle("old");
        existing.setDescription("old-desc");

        Exam payload = new Exam();
        payload.setTitle("new-title");

        when(examRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Exam updated = examService.update(1L, payload);

        assertEquals("new-title", updated.getTitle());
        assertEquals("old-desc", updated.getDescription());
    }

    @Test
    void findByIdShouldThrowWhenNotFound() {
        when(examRepository.findById(42L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> examService.findById(42L));

        assertEquals("Exam not found with id: 42", ex.getMessage());
    }

    @Test
    void deleteShouldThrowWhenNotFound() {
        when(examRepository.existsById(55L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> examService.delete(55L));

        assertEquals("Exam not found with id: 55", ex.getMessage());
    }

    @Test
    void deleteShouldCallRepositoryWhenExists() {
        when(examRepository.existsById(5L)).thenReturn(true);

        examService.delete(5L);

        verify(examRepository).deleteById(5L);
    }
}
