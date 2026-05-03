package tn.esprit.examquizservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.examquizservice.dtos.ResultResponse;
import tn.esprit.examquizservice.dtos.SubmitResultRequest;
import tn.esprit.examquizservice.services.ResultService;

import java.util.List;

@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    /**
     * POST /api/results/submit
     * Persists a new result derived from a completed attempt.
     */
    @PostMapping("/submit")
    public ResponseEntity<ResultResponse> submit(@RequestBody SubmitResultRequest request) {
        return ResponseEntity.ok(resultService.submit(request));
    }

    /**
     * GET /api/results/me?examId=...&userId=...
     * Returns the result for the authenticated user on a given exam.
     */
    @GetMapping("/me")
    public ResponseEntity<ResultResponse> getMyResult(
            @RequestParam Long userId,
            @RequestParam Long examId) {
        return ResponseEntity.ok(resultService.getMyResult(userId, examId));
    }

    /**
     * GET /api/results/by-attempt/{attemptId}
     * Returns the result linked to a specific attempt.
     */
    @GetMapping("/by-attempt/{attemptId}")
    public ResponseEntity<ResultResponse> getByAttempt(@PathVariable Long attemptId) {
        return ResponseEntity.ok(resultService.getByAttempt(attemptId));
    }

    /**
     * GET /api/results/history/me?userId=...
     * Returns the full result history for a user.
     */
    @GetMapping("/history/me")
    public ResponseEntity<List<ResultResponse>> getHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(resultService.getHistory(userId));
    }

    /**
     * GET /api/results?examId=&userId= (admin view)
     */
    @GetMapping
    public ResponseEntity<List<ResultResponse>> getByExamAndUser(
            @RequestParam Long examId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(resultService.getByExamAndUser(examId, userId));
    }

    /**
     * PATCH /api/results/increase-score?userId=...&deltaPoints=...&examId=...
     * Increases a user's earned score by points.
     * - examId optional; if omitted, latest user result is updated.
     */
    @PatchMapping("/increase-score")
    public ResponseEntity<ResultResponse> increaseScore(
            @RequestParam Long userId,
            @RequestParam Double deltaPoints,
            @RequestParam(required = false) Long examId) {
        return ResponseEntity.ok(resultService.increaseScoreByUserId(userId, deltaPoints, examId));
    }
}
