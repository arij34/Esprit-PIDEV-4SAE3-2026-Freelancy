package tn.esprit.examquizservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tn.esprit.examquizservice.services.AdminMonitoringService;

@RestController
@RequestMapping("/api/admin/monitoring")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AdminMonitoringController {

    private final AdminMonitoringService adminMonitoringService;

    @GetMapping("/exams/{examId}/snapshot")
    public ResponseEntity<?> getExamSnapshot(@PathVariable Long examId) {
        log.debug("Fetching live monitoring snapshot for exam {}", examId);
        return ResponseEntity.ok(adminMonitoringService.getLiveSnapshot(examId));
    }

    @GetMapping("/exams/{examId}/events")
    public ResponseEntity<?> getRecentEvents(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "30") int sinceMinutes,
            @RequestParam(defaultValue = "25") int limit) {
        log.debug("Fetching recent monitoring events for exam {}", examId);
        return ResponseEntity.ok(adminMonitoringService.getRecentEvents(examId, sinceMinutes, limit));
    }

    @GetMapping(path = "/exams/{examId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamExamMonitoring(@PathVariable Long examId) {
        log.info("Opening admin monitoring stream for exam {}", examId);
        return adminMonitoringService.streamExamMonitoring(examId);
    }

    @GetMapping("/exams/{examId}/candidates/{userId}/activity")
    public ResponseEntity<?> getCandidateActivity(
            @PathVariable Long examId,
            @PathVariable Long userId) {
        log.debug("Fetching candidate activity for exam {} user {}", examId, userId);
        return ResponseEntity.ok(adminMonitoringService.getCandidateActivity(examId, userId));
    }
}