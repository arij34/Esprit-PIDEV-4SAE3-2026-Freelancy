package tn.esprit.examquizservice.services;

import tn.esprit.examquizservice.entities.ExamParticipation;

import java.util.List;

public interface IExamParticipationService {

    ExamParticipation joinExam(Long examId, String authorization);

    List<ExamParticipation> getMyParticipations(String authorization);

    ExamParticipation getMyParticipationForExam(Long examId, String authorization);

    List<ExamParticipation> getAllParticipations();

    List<ExamParticipation> getParticipationsByExam(Long examId);

    ExamParticipation getParticipationById(Long id);

    void deleteParticipation(Long id);

    long getTotalParticipantsCount();

    long getParticipantCountForExam(Long examId);
}
