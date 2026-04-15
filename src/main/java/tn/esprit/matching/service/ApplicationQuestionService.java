package tn.esprit.matching.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.matching.entity.ApplicationQuestion;
import tn.esprit.matching.repository.ApplicationQuestionRepository;

import java.util.List;

@Service
public class ApplicationQuestionService {

    @Autowired
    private ApplicationQuestionRepository applicationQuestionRepository;

    public List<ApplicationQuestion> getQuestionsForInvitation(Long invitationId) {
        // pour le moment on ignore invitationId et on renvoie les questions globales
        return applicationQuestionRepository.findAllByOrderByOrderIndexAsc();
    }
}