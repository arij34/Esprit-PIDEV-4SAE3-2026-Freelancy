package tn.esprit.examquizservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.examquizservice.entities.Answer;
import tn.esprit.examquizservice.entities.Attempt;
import tn.esprit.examquizservice.entities.AttemptAnswer;
import tn.esprit.examquizservice.entities.CheatingLog;
import tn.esprit.examquizservice.entities.ExamSetting;
import tn.esprit.examquizservice.entities.QuizSetting;
import tn.esprit.examquizservice.repositories.AnswerRepository;
import tn.esprit.examquizservice.repositories.AttemptAnswerRepository;
import tn.esprit.examquizservice.repositories.AttemptRepository;
import tn.esprit.examquizservice.repositories.CheatingLogRepository;
import tn.esprit.examquizservice.repositories.ExamSettingRepository;
import tn.esprit.examquizservice.repositories.QuizSettingRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleCrudServicesTest {

    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private AttemptRepository attemptRepository;
    @Mock
    private AttemptAnswerRepository attemptAnswerRepository;
    @Mock
    private CheatingLogRepository cheatingLogRepository;
    @Mock
    private ExamSettingRepository examSettingRepository;
    @Mock
    private QuizSettingRepository quizSettingRepository;

    @Test
    void answerServiceUpdateShouldKeepExistingId() {
        AnswerService service = new AnswerService(answerRepository);
        Answer existing = new Answer();
        existing.setId(3L);
        Answer payload = new Answer();
        payload.setId(99L);

        when(answerRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(answerRepository.save(payload)).thenReturn(payload);

        service.update(3L, payload);

        assertEquals(3L, payload.getId());
    }

    @Test
    void attemptServiceFindByIdShouldThrowWhenMissing() {
        AttemptService service = new AttemptService(attemptRepository);
        when(attemptRepository.findById(8L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.findById(8L));

        assertEquals("Attempt not found with id: 8", ex.getMessage());
    }

    @Test
    void attemptAnswerServiceUpdateShouldKeepExistingId() {
        AttemptAnswerService service = new AttemptAnswerService(attemptAnswerRepository);
        AttemptAnswer existing = new AttemptAnswer();
        existing.setId(6L);
        AttemptAnswer payload = new AttemptAnswer();

        when(attemptAnswerRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(attemptAnswerRepository.save(payload)).thenReturn(payload);

        service.update(6L, payload);

        assertEquals(6L, payload.getId());
    }

    @Test
    void cheatingLogServiceFindByIdShouldThrowWhenMissing() {
        CheatingLogService service = new CheatingLogService(cheatingLogRepository);
        when(cheatingLogRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.findById(2L));

        assertEquals("Cheating log not found with id: 2", ex.getMessage());
    }

    @Test
    void examSettingServiceShouldReturnNullWhenExamSettingAbsent() {
        ExamSettingService service = new ExamSettingService(examSettingRepository);
        when(examSettingRepository.findByExam_Id(11L)).thenReturn(Optional.empty());

        assertEquals(null, service.findByExamId(11L));
    }

    @Test
    void examSettingServiceUpdateShouldKeepExistingId() {
        ExamSettingService service = new ExamSettingService(examSettingRepository);
        ExamSetting existing = new ExamSetting();
        existing.setId(5L);
        ExamSetting payload = new ExamSetting();

        when(examSettingRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(examSettingRepository.save(payload)).thenReturn(payload);

        service.update(5L, payload);

        assertEquals(5L, payload.getId());
    }

    @Test
    void quizSettingServiceFindByIdShouldThrowWhenMissing() {
        QuizSettingService service = new QuizSettingService(quizSettingRepository);
        when(quizSettingRepository.findById(7L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.findById(7L));

        assertEquals("Quiz setting not found with id: 7", ex.getMessage());
    }

    @Test
    void deleteMethodsShouldDelegate() {
        AnswerService answerService = new AnswerService(answerRepository);
        AttemptService attemptService = new AttemptService(attemptRepository);
        AttemptAnswerService attemptAnswerService = new AttemptAnswerService(attemptAnswerRepository);
        CheatingLogService cheatingLogService = new CheatingLogService(cheatingLogRepository);
        ExamSettingService examSettingService = new ExamSettingService(examSettingRepository);
        QuizSettingService quizSettingService = new QuizSettingService(quizSettingRepository);

        answerService.delete(1L);
        attemptService.delete(2L);
        attemptAnswerService.delete(3L);
        cheatingLogService.delete(4L);
        examSettingService.delete(5L);
        quizSettingService.delete(6L);

        verify(answerRepository).deleteById(1L);
        verify(attemptRepository).deleteById(2L);
        verify(attemptAnswerRepository).deleteById(3L);
        verify(cheatingLogRepository).deleteById(4L);
        verify(examSettingRepository).deleteById(5L);
        verify(quizSettingRepository).deleteById(6L);
    }
}
