package tn.esprit.examquizservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examquizservice.entities.ExamParticipation;
import tn.esprit.examquizservice.services.IExamParticipationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-participations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ExamParticipationController {

    private final IExamParticipationService participationService;

    /**
     * Join an exam (requires Authorization header with JWT bearer token).
     * POST /api/exam-participations/{examId}/join
     */
    @PostMapping("/{examId}/join")
    public ResponseEntity<ExamParticipation> joinExam(
            @PathVariable Long examId,
            @RequestHeader("Authorization") String authorization) {
        log.info("User joining exam {}", examId);
        ExamParticipation participation = participationService.joinExam(examId, authorization);
        return ResponseEntity.ok(participation);
    }

    /**
     * Get all exams the current user has joined.
     * GET /api/exam-participations/my/exams
     */
    @GetMapping("/my/exams")
    public ResponseEntity<List<ExamParticipation>> getMyParticipations(
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(participationService.getMyParticipations(authorization));
    }

    /**
     * Get the current user's participation for a specific exam.
     * GET /api/exam-participations/my/exam/{examId}
     */
    @GetMapping("/my/exam/{examId}")
    public ResponseEntity<ExamParticipation> getMyParticipationForExam(
            @PathVariable Long examId,
            @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(participationService.getMyParticipationForExam(examId, authorization));
    }

    /**
     * [Admin] Get all participations across all exams.
     * GET /api/exam-participations
     */
    @GetMapping
    public ResponseEntity<List<ExamParticipation>> getAllParticipations() {
        return ResponseEntity.ok(participationService.getAllParticipations());
    }

    /**
     * [Admin] Get all participations for a specific exam.
     * GET /api/exam-participations/exam/{examId}
     */
    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<ExamParticipation>> getParticipationsByExam(@PathVariable Long examId) {
        return ResponseEntity.ok(participationService.getParticipationsByExam(examId));
    }

    /**
     * Get a specific participation by its ID.
     * GET /api/exam-participations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExamParticipation> getParticipationById(@PathVariable Long id) {
        return ResponseEntity.ok(participationService.getParticipationById(id));
    }

    /**
     * Delete a participation.
     * DELETE /api/exam-participations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParticipation(@PathVariable Long id) {
        participationService.deleteParticipation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get total participant count across all exams.
     * GET /api/exam-participations/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getTotalCount() {
        return ResponseEntity.ok(Map.of("count", participationService.getTotalParticipantsCount()));
    }

    /**
     * Get participant count for a specific exam.
     * GET /api/exam-participations/exam/{examId}/count
     */
    @GetMapping("/exam/{examId}/count")
    public ResponseEntity<Map<String, Long>> getCountForExam(@PathVariable Long examId) {
        return ResponseEntity.ok(Map.of("count", participationService.getParticipantCountForExam(examId)));
    }
}
