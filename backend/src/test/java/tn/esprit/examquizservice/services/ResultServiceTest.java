package tn.esprit.examquizservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.examquizservice.clients.UserServiceClient;
import tn.esprit.examquizservice.dtos.ResultResponse;
import tn.esprit.examquizservice.dtos.SubmitResultRequest;
import tn.esprit.examquizservice.entities.Exam;
import tn.esprit.examquizservice.entities.ExamResult;
import tn.esprit.examquizservice.entities.ResultStatus;
import tn.esprit.examquizservice.exceptions.DuplicateResultException;
import tn.esprit.examquizservice.exceptions.ResourceNotFoundException;
import tn.esprit.examquizservice.repositories.ExamRepository;
import tn.esprit.examquizservice.repositories.ExamResultRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ExamResultRepository resultRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ResultService resultService;

    @Test
    void submitShouldRejectDuplicateAttempt() {
        SubmitResultRequest request = SubmitResultRequest.builder()
                .attemptId(10L)
                .userId(1L)
                .examId(2L)
                .build();

        when(resultRepository.findByAttemptId(10L)).thenReturn(Optional.of(new ExamResult()));

        assertThrows(DuplicateResultException.class, () -> resultService.submit(request));
        verify(resultRepository, never()).save(any(ExamResult.class));
    }

    @Test
    void submitShouldComputeFailedStatusAndFallbackToToStringForAnswers() throws Exception {
        SubmitResultRequest request = SubmitResultRequest.builder()
                .attemptId(11L)
                .userId(2L)
                .examId(7L)
                .earnedPoints(20.0)
                .totalPoints(100.0)
                .autoSubmitted(false)
                .answers(Map.of(1L, "A"))
                .build();

        Exam exam = new Exam();
        exam.setId(7L);
        exam.setPassingScore(60.0);

        when(resultRepository.findByAttemptId(11L)).thenReturn(Optional.empty());
        when(resultRepository.findByUserIdAndExamId(2L, 7L)).thenReturn(Optional.empty());
        when(examRepository.findById(7L)).thenReturn(Optional.of(exam));
        when(objectMapper.writeValueAsString(request.getAnswers())).thenThrow(new JsonProcessingException("boom") {});
        when(resultRepository.save(any(ExamResult.class))).thenAnswer(invocation -> {
            ExamResult r = invocation.getArgument(0);
            r.setId(99L);
            return r;
        });

        ResultResponse response = resultService.submit(request);

        assertEquals(20.0, response.getScorePercent());
        assertEquals(ResultStatus.FAILED, response.getStatus());
        assertTrue(response.getAnswersJson().contains("1=A"));
        verify(userServiceClient).addExperiencePoints(2L, 20.0);
    }

    @Test
    void submitShouldSetAutoSubmittedStatus() throws Exception {
        SubmitResultRequest request = SubmitResultRequest.builder()
                .attemptId(12L)
                .userId(3L)
                .examId(9L)
                .earnedPoints(25.0)
                .autoSubmitted(true)
                .build();

        Exam exam = new Exam();
        exam.setId(9L);
        exam.setPoints(50.0);

        when(resultRepository.findByAttemptId(12L)).thenReturn(Optional.empty());
        when(resultRepository.findByUserIdAndExamId(3L, 9L)).thenReturn(Optional.empty());
        when(examRepository.findById(9L)).thenReturn(Optional.of(exam));
        when(resultRepository.save(any(ExamResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResultResponse response = resultService.submit(request);

        assertEquals(ResultStatus.AUTO_SUBMITTED, response.getStatus());
        assertEquals(50.0, response.getScorePercent());
    }

    @Test
    void saveOrGetShouldReturnExistingResult() {
        ExamResult existing = ExamResult.builder().id(1L).attemptId(22L).status(ResultStatus.PASSED).build();
        when(resultRepository.findByAttemptId(22L)).thenReturn(Optional.of(existing));

        ResultResponse response = resultService.saveOrGet(1L, 1L, 22L, 40.0, 50.0, false, 120, 50.0);

        assertEquals(1L, response.getId());
        verify(resultRepository, never()).save(any(ExamResult.class));
    }

    @Test
    void increaseScoreShouldValidateDeltaPoints() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> resultService.increaseScoreByUserId(1L, 0.0, 1L));

        assertEquals("deltaPoints must be > 0", ex.getMessage());
    }

    @Test
    void increaseScoreShouldClampAndRecomputeStatus() {
        ExamResult result = ExamResult.builder()
                .id(7L)
                .examId(11L)
                .userId(4L)
                .earnedPoints(80.0)
                .totalPoints(100.0)
                .status(ResultStatus.FAILED)
                .submittedAt(LocalDateTime.now())
                .build();

        Exam exam = new Exam();
        exam.setPassingScore(75.0);

        when(resultRepository.findByUserIdAndExamId(4L, 11L)).thenReturn(Optional.of(result));
        when(examRepository.findById(11L)).thenReturn(Optional.of(exam));
        when(resultRepository.save(any(ExamResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResultResponse response = resultService.increaseScoreByUserId(4L, 50.0, 11L);

        assertEquals(100.0, response.getEarnedPoints());
        assertEquals(100.0, response.getScorePercent());
        assertEquals(ResultStatus.PASSED, response.getStatus());
    }

    @Test
    void increaseScoreShouldKeepAutoSubmittedStatus() {
        ExamResult result = ExamResult.builder()
                .examId(6L)
                .userId(4L)
                .earnedPoints(10.0)
                .totalPoints(20.0)
                .status(ResultStatus.AUTO_SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .build();

        when(resultRepository.findTopByUserIdOrderBySubmittedAtDescIdDesc(4L)).thenReturn(Optional.of(result));
        when(resultRepository.save(any(ExamResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResultResponse response = resultService.increaseScoreByUserId(4L, 5.0, null);

        assertEquals(ResultStatus.AUTO_SUBMITTED, response.getStatus());
        verify(examRepository, never()).findById(any());
    }

    @Test
    void getMyResultShouldThrowWhenMissing() {
        when(resultRepository.findByUserIdAndExamId(50L, 70L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resultService.getMyResult(50L, 70L));
    }

    @Test
    void getHistoryAndGetByExamAndUserShouldMapResponses() {
        ExamResult item = ExamResult.builder().id(1L).examId(2L).userId(3L).status(ResultStatus.PASSED).build();
        when(resultRepository.findByUserId(3L)).thenReturn(List.of(item));
        when(resultRepository.findAllByUserIdAndExamId(3L, 2L)).thenReturn(List.of(item));

        List<ResultResponse> history = resultService.getHistory(3L);
        List<ResultResponse> filtered = resultService.getByExamAndUser(2L, 3L);

        assertEquals(1, history.size());
        assertEquals(1, filtered.size());
        assertEquals(ResultStatus.PASSED, filtered.get(0).getStatus());
    }
}
