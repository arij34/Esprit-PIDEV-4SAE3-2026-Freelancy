package tn.esprit.examquizservice.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.examquizservice.clients.UserDto;
import tn.esprit.examquizservice.clients.UserServiceClient;
import tn.esprit.examquizservice.entities.Exam;
import tn.esprit.examquizservice.entities.ExamParticipation;
import tn.esprit.examquizservice.entities.ExamParticipationStatus;
import tn.esprit.examquizservice.exceptions.ResourceNotFoundException;
import tn.esprit.examquizservice.repositories.ExamParticipationRepository;
import tn.esprit.examquizservice.repositories.ExamRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamParticipationServiceImplTest {

    @Mock
    private ExamParticipationRepository participationRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ExamParticipationServiceImpl service;

    @Test
    void joinExamShouldReturnExistingParticipation() {
        UserDto user = new UserDto(7L, "John", "Doe", "john@x.com", "STUDENT", true);
        ExamParticipation existing = ExamParticipation.builder().id(99L).build();

        when(userServiceClient.getCurrentUser("Bearer token")).thenReturn(user);
        when(participationRepository.findByExam_IdAndUserId(1L, 7L)).thenReturn(Optional.of(existing));

        ExamParticipation result = service.joinExam(1L, "Bearer token");

        assertEquals(99L, result.getId());
        verify(participationRepository, never()).save(any(ExamParticipation.class));
    }

    @Test
    void joinExamShouldCreateParticipationWhenFirstJoin() {
        UserDto user = new UserDto(7L, "John", "Doe", "john@x.com", "STUDENT", true);
        Exam exam = new Exam();
        exam.setId(1L);

        when(userServiceClient.getCurrentUser("Bearer token")).thenReturn(user);
        when(participationRepository.findByExam_IdAndUserId(1L, 7L)).thenReturn(Optional.empty());
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(participationRepository.save(any(ExamParticipation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExamParticipation result = service.joinExam(1L, "Bearer token");

        assertEquals(7L, result.getUserId());
        assertEquals(ExamParticipationStatus.REGISTERED, result.getStatus());
        assertEquals(exam, result.getExam());
    }

    @Test
    void getMyParticipationForExamShouldThrowWhenMissing() {
        UserDto user = new UserDto(4L, "A", "B", "a@b.com", "STUDENT", true);
        when(userServiceClient.getCurrentUser("Bearer x")).thenReturn(user);
        when(participationRepository.findByExam_IdAndUserId(10L, 4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getMyParticipationForExam(10L, "Bearer x"));
    }

    @Test
    void deleteParticipationShouldThrowWhenMissing() {
        when(participationRepository.existsById(12L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.deleteParticipation(12L));

        assertEquals("Participation not found with id: 12", ex.getMessage());
    }

    @Test
    void countMethodsShouldDelegate() {
        when(participationRepository.count()).thenReturn(20L);
        when(participationRepository.countByExam_Id(9L)).thenReturn(5L);

        assertEquals(20L, service.getTotalParticipantsCount());
        assertEquals(5L, service.getParticipantCountForExam(9L));
    }

    @Test
    void readMethodsShouldDelegate() {
        when(participationRepository.findAll()).thenReturn(List.of(new ExamParticipation()));
        when(participationRepository.findByExam_Id(2L)).thenReturn(List.of(new ExamParticipation(), new ExamParticipation()));
        when(participationRepository.findById(1L)).thenReturn(Optional.of(new ExamParticipation()));

        assertEquals(1, service.getAllParticipations().size());
        assertEquals(2, service.getParticipationsByExam(2L).size());
        assertEquals(new ExamParticipation().getClass(), service.getParticipationById(1L).getClass());
    }
}
