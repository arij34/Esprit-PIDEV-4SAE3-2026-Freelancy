package tn.esprit.examquizservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import tn.esprit.examquizservice.entities.ExamParticipation;
import tn.esprit.examquizservice.entities.ExamParticipationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamParticipationRepository extends JpaRepository<ExamParticipation, Long> {

    @Override
    @EntityGraph(attributePaths = {"exam"})
    Optional<ExamParticipation> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"exam"})
    List<ExamParticipation> findAll();

    boolean existsByExam_IdAndUserId(Long examId, Long userId);

    @EntityGraph(attributePaths = {"exam"})
    Optional<ExamParticipation> findByExam_IdAndUserId(Long examId, Long userId);

    @EntityGraph(attributePaths = {"exam"})
    List<ExamParticipation> findByExam_Id(Long examId);

    @EntityGraph(attributePaths = {"exam"})
    List<ExamParticipation> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"exam"})
    List<ExamParticipation> findByStatus(ExamParticipationStatus status);

    long countByExam_Id(Long examId);
}
