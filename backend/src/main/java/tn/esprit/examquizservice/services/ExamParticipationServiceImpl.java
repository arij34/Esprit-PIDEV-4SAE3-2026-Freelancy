package tn.esprit.examquizservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.examquizservice.clients.UserDto;
import tn.esprit.examquizservice.clients.UserServiceClient;
import tn.esprit.examquizservice.entities.Exam;
import tn.esprit.examquizservice.entities.ExamParticipation;
import tn.esprit.examquizservice.entities.ExamParticipationStatus;
import tn.esprit.examquizservice.exceptions.ResourceNotFoundException;
import tn.esprit.examquizservice.repositories.ExamParticipationRepository;
import tn.esprit.examquizservice.repositories.ExamRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamParticipationServiceImpl implements IExamParticipationService {

    private final ExamParticipationRepository participationRepository;
    private final ExamRepository examRepository;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public ExamParticipation joinExam(Long examId, String authorization) {
        UserDto currentUser = userServiceClient.getCurrentUser(authorization);
        Long userId = currentUser.getId();

        // Idempotent join: if user already joined this exam, return existing participation.
        java.util.Optional<ExamParticipation> existing = participationRepository.findByExam_IdAndUserId(examId, userId);
        if (existing.isPresent()) {
            log.info("User {} already joined exam {} - returning existing participation {}",
                userId, examId, existing.get().getId());
            return existing.get();
        }

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found with id: " + examId));

        ExamParticipation participation = ExamParticipation.builder()
            .userId(userId)
                .userFirstName(currentUser.getFirstName())
                .userLastName(currentUser.getLastName())
                .userEmail(currentUser.getEmail())
                .exam(exam)
                .status(ExamParticipationStatus.REGISTERED)
                .joinedAt(LocalDateTime.now())
                .build();

        ExamParticipation saved = participationRepository.save(participation);
        log.info("User {} ({}) joined exam {}", currentUser.getId(), currentUser.getEmail(), examId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamParticipation> getMyParticipations(String authorization) {
        UserDto currentUser = userServiceClient.getCurrentUser(authorization);
        return participationRepository.findByUserId(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ExamParticipation getMyParticipationForExam(Long examId, String authorization) {
        UserDto currentUser = userServiceClient.getCurrentUser(authorization);
        return participationRepository.findByExam_IdAndUserId(examId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You have not joined this exam"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamParticipation> getAllParticipations() {
        return participationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamParticipation> getParticipationsByExam(Long examId) {
        return participationRepository.findByExam_Id(examId);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamParticipation getParticipationById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participation not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteParticipation(Long id) {
        if (!participationRepository.existsById(id)) {
            throw new RuntimeException("Participation not found with id: " + id);
        }
        participationRepository.deleteById(id);
        log.info("Deleted exam participation id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalParticipantsCount() {
        return participationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long getParticipantCountForExam(Long examId) {
        return participationRepository.countByExam_Id(examId);
    }
}
