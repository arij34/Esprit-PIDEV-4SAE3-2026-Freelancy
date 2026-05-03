package tn.esprit.examquizservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.examquizservice.dtos.StartExamRequest;
import tn.esprit.examquizservice.dtos.StartExamResponse;
import tn.esprit.examquizservice.dtos.SubmitExamRequest;
import tn.esprit.examquizservice.dtos.SubmitExamResponse;
import tn.esprit.examquizservice.entities.Answer;
import tn.esprit.examquizservice.entities.Attempt;
import tn.esprit.examquizservice.entities.AttemptAnswer;
import tn.esprit.examquizservice.entities.AttemptStatus;
import tn.esprit.examquizservice.entities.Exam;
import tn.esprit.examquizservice.entities.ExamSetting;
import tn.esprit.examquizservice.entities.Question;
import tn.esprit.examquizservice.repositories.AttemptRepository;
import tn.esprit.examquizservice.repositories.CheatingLogRepository;
import tn.esprit.examquizservice.repositories.ExamRepository;
import tn.esprit.examquizservice.repositories.ExamSettingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamSessionServiceTest {

    @Mock
    private AttemptRepository attemptRepository;
    @Mock
    private ExamRepository examRepository;
    @Mock
    private ExamSettingRepository examSettingRepository;
    @Mock
    private ResultService resultService;
    @Mock
    private AntiCheatingService antiCheatingService;
    @Mock
    private QuestionService questionService;
    @Mock
    private AnswerService answerService;
    @Mock
    private AttemptAnswerService attemptAnswerService;
    @Mock
    private CheatingLogRepository cheatingLogRepository;

    @InjectMocks
    private ExamSessionService service;

    @Test
    void startExamShouldReturnMessageWhenExamNotFound() {
        StartExamRequest request = StartExamRequest.builder().userId(1L).examId(2L).build();

        when(attemptRepository.findByUserId(1L)).thenReturn(List.of(), List.of());
        when(examRepository.findById(2L)).thenReturn(Optional.empty());

        StartExamResponse response = service.startExam(request);

        assertEquals("Exam not found", response.getMessage());
    }

    @Test
    void startExamShouldReuseActiveAttemptForSameExam() {
        StartExamRequest request = StartExamRequest.builder().userId(1L).examId(2L).build();

        Exam exam = new Exam();
        exam.setId(2L);
        exam.setTitle("Java Exam");
        exam.setDuration(45);
        exam.setPoints(100.0);

        Attempt active = new Attempt();
        active.setId(10L);
        active.setStatus(AttemptStatus.IN_PROGRESS);
        active.setExam(exam);
        active.setSessionToken("session-1");
        active.setStartTime(LocalDateTime.now().minusMinutes(2));
        active.setExpectedEndTime(System.currentTimeMillis() + 10000);

        when(attemptRepository.findByUserId(1L)).thenReturn(List.of(active), List.of(active));
        when(examRepository.findById(2L)).thenReturn(Optional.of(exam));
        when(questionService.findByExamId(2L)).thenReturn(List.of(new Question(), new Question()));

        StartExamResponse response = service.startExam(request);

        assertEquals("Active exam session resumed", response.getMessage());
        assertEquals(10L, response.getAttemptId());
        assertEquals("session-1", response.getSessionToken());
        assertEquals(2, response.getQuestionCount());
        verify(attemptRepository, never()).save(any(Attempt.class));
    }

    @Test
    void startExamShouldCreateAttemptAndNormalizeFingerprint() {
        String longFingerprint = "x".repeat(300);
        StartExamRequest request = StartExamRequest.builder()
                .userId(3L)
                .examId(9L)
                .ipAddress("1.1.1.1")
                .deviceFingerprint(longFingerprint)
                .browserInfo("Chrome")
                .build();

        Exam exam = new Exam();
        exam.setId(9L);
        exam.setTitle("Algorithms");
        exam.setDuration(30);
        exam.setPoints(20.0);

        Attempt saved = new Attempt();
        saved.setId(123L);

        when(attemptRepository.findByUserId(3L)).thenReturn(List.of(), List.of(), List.of());
        when(examRepository.findById(9L)).thenReturn(Optional.of(exam));
        when(examSettingRepository.findByExam_Id(9L)).thenReturn(Optional.of(new ExamSetting()));
        when(questionService.findByExamId(9L)).thenReturn(List.of(new Question()));
        when(attemptRepository.save(any(Attempt.class))).thenReturn(saved);

        StartExamResponse response = service.startExam(request);

        ArgumentCaptor<Attempt> captor = ArgumentCaptor.forClass(Attempt.class);
        verify(attemptRepository).save(captor.capture());
        Attempt toSave = captor.getValue();

        assertEquals(123L, response.getAttemptId());
        assertEquals(255, toSave.getDeviceFingerprint().length());
        assertEquals(AttemptStatus.IN_PROGRESS, toSave.getStatus());
        assertEquals("Exam session started successfully", response.getMessage());
    }

    @Test
    void submitExamShouldReturnNotFoundWhenAttemptCannotBeResolved() {
        SubmitExamRequest request = SubmitExamRequest.builder().attemptId(99L).sessionToken("s").build();

        when(antiCheatingService.validateSessionToken("s", null, null)).thenReturn(true);
        when(attemptRepository.findById(99L)).thenReturn(Optional.empty());
        when(attemptRepository.findBySessionToken("s")).thenReturn(Optional.empty());

        SubmitExamResponse response = service.submitExam(request);

        assertEquals("Attempt not found", response.getMessage());
    }

    @Test
    void saveAnswerShouldIncrementTooFastAndPersistAttemptAnswer() {
        Attempt attempt = new Attempt();
        attempt.setId(20L);

        Question question = new Question();
        question.setId(5L);

        when(attemptRepository.findById(20L)).thenReturn(Optional.of(attempt));
        when(antiCheatingService.checkTooFastAnswer(2)).thenReturn(true);
        when(questionService.getById(5L)).thenReturn(Optional.of(question));

        service.saveAnswer(20L, 5L, "A", 2);

        assertEquals(1, attempt.getTooFastAnswerCount());
        verify(attemptAnswerService).create(any(AttemptAnswer.class));
        verify(attemptRepository).save(attempt);
    }

    @Test
    void autoSubmitExamShouldSetAutoSubmittedAndReturnComputedScore() {
        Exam exam = new Exam();
        exam.setId(1L);
        exam.setPoints(20.0);
        exam.setPassingScore(10.0);

        Question q = new Question();
        q.setPoints(20.0);

        AttemptAnswer aa = new AttemptAnswer();
        aa.setQuestion(q);

        Attempt attempt = new Attempt();
        attempt.setId(5L);
        attempt.setExam(exam);
        attempt.setUserId(11L);
        attempt.setAttemptAnswers(List.of(aa));

        when(attemptRepository.findById(5L)).thenReturn(Optional.of(attempt));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(resultService.saveOrGet(anyLong(), anyLong(), anyLong(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        SubmitExamResponse response = service.autoSubmitExam(5L);

        assertEquals("AUTO_SUBMITTED", response.getStatus());
        assertTrue(response.getAutoSubmitted());
        assertEquals(20.0, response.getScore());
    }

    @Test
    void autoSubmitExamShouldReturnMessageWhenAttemptMissing() {
        when(attemptRepository.findById(404L)).thenReturn(Optional.empty());

        SubmitExamResponse response = service.autoSubmitExam(404L);

        assertEquals("Attempt not found", response.getMessage());
    }
}
