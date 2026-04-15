package tn.esprit.matching.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.matching.entity.ApplicationQuestion;
import tn.esprit.matching.service.ApplicationQuestionService;

import java.util.List;

@RestController
@RequestMapping("/application-questions")
@CrossOrigin(origins = "http://localhost:4200")
public class ApplicationQuestionController {

    @Autowired
    private ApplicationQuestionService applicationQuestionService;

    @GetMapping("/invitation/{invitationId}")
    public ResponseEntity<List<ApplicationQuestion>> getForInvitation(
            @PathVariable Long invitationId) {
        return ResponseEntity.ok(applicationQuestionService.getQuestionsForInvitation(invitationId));
    }
}