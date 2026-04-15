package tn.esprit.matching.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.matching.clients.InvitationProjectDTO;
import tn.esprit.matching.clients.ProjectClient;
import tn.esprit.matching.dto.FormResponseRequest;
import tn.esprit.matching.entity.ApplicationResponse;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.repository.ApplicationResponseRepository;
import tn.esprit.matching.repository.InvitationRepository;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ApplicationResponseService {

    private static final long EDIT_WINDOW_HOURS = 24;

    @Autowired
    private ApplicationResponseRepository applicationResponseRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private ProjectClient projectClient;

    public ApplicationResponse saveResponse(FormResponseRequest request) {
        // 1) Charger l'invitation
        Invitation inv = invitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        // 2) Charger infos projet pour validation (budgetMax, durationEstimatedWeeks)
        Integer budgetMax = null;
        Integer durationEstimatedWeeks = null;
        try {
            InvitationProjectDTO project =
                    projectClient.getInvitationData(inv.getProjectId());
            if (project != null) {
                budgetMax = project.getBudgetMax();
                durationEstimatedWeeks = project.getDurationEstimatedWeeks();
            }
        } catch (Exception e) {
            System.err.println("Erreur récupération projet pour validation formulaire: " + e.getMessage());
        }

        // 3) Validations texte
        if (request.getQ1() != null && request.getQ1().length() > 2000) {
            throw new IllegalArgumentException("Answer Q1 must not exceed 2000 characters");
        }
        if (request.getQ2() != null && request.getQ2().length() > 2000) {
            throw new IllegalArgumentException("Answer Q2 must not exceed 2000 characters");
        }
        if (request.getQ3() != null && request.getQ3().length() > 2000) {
            throw new IllegalArgumentException("Answer Q3 must not exceed 2000 characters");
        }
        if (request.getQ5() != null && request.getQ5().length() > 2000) {
            throw new IllegalArgumentException("Answer Q5 must not exceed 2000 characters");
        }

        // 4) Validation métier budget / timeline
        validateBudget(request.getQ4(), budgetMax);
        validateTimeline(request.getQ3(), durationEstimatedWeeks);

        // 5) Charger ou créer la réponse
        ApplicationResponse ar = applicationResponseRepository
                .findByInvitationId(inv.getId())
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        if (ar != null) {
            // 🔹 Modification : vérifier fenêtre de 24h
            if (ar.getCreatedAt() != null) {
                long hours = Duration.between(ar.getCreatedAt(), now).toHours();
                if (hours > EDIT_WINDOW_HOURS) {
                    throw new IllegalStateException(
                            "You can no longer edit your answers after 24 hours."
                    );
                }
            }
        } else {
            // 🔹 Première soumission : création de la réponse
            ar = new ApplicationResponse();
            ar.setInvitationId(inv.getId());
            ar.setFreelancerId(inv.getFreelancerId());
            ar.setProjectId(inv.getProjectId());
        }

        // 6) Mettre à jour les réponses
        ar.setAnswerQ1(request.getQ1());
        ar.setAnswerQ2(request.getQ2());
        ar.setAnswerQ3(request.getQ3());
        ar.setAnswerQ4(request.getQ4());
        ar.setAnswerQ5(request.getQ5());

        return applicationResponseRepository.save(ar);
    }

    public ApplicationResponse getByInvitationId(Long invitationId) {
        return applicationResponseRepository
                .findByInvitationId(invitationId)
                .orElse(null);
    }

    public boolean canEdit(Long invitationId) {
        ApplicationResponse ar = applicationResponseRepository
                .findByInvitationId(invitationId)
                .orElse(null);
        if (ar == null || ar.getCreatedAt() == null) {
            return true; // pas encore de réponse → éditable
        }
        long hours = Duration.between(ar.getCreatedAt(), LocalDateTime.now()).toHours();
        return hours <= EDIT_WINDOW_HOURS;
    }
    /**
     * Q4 : budget proposé.
     * - peut être vide
     * - peut être du texte "à discuter", "negotiable"
     * - si contient un nombre, ce nombre ne doit pas dépasser budgetMax (si connu)
     */
    private void validateBudget(String q4, Integer budgetMax) {
        if (q4 == null || q4.trim().isEmpty()) {
            // budget laissé vide → OK
            return;
        }

        String trimmed = q4.trim().toLowerCase();

        // texte libre pour dire "on verra plus tard"
        if (trimmed.contains("discut") || trimmed.contains("nego")
                || trimmed.contains("discuss") || trimmed.contains("later")) {
            return;
        }

        // Essayer d'extraire un nombre (ex: "2500", "2500 USD", "2 500 TND")
        String digits = trimmed.replaceAll("[^0-9.]", "");
        if (digits.isEmpty()) {
            // aucun nombre → on accepte comme texte libre
            return;
        }

        try {
            double amount = Double.parseDouble(digits);

            if (budgetMax != null && amount > budgetMax) {
                throw new IllegalArgumentException(
                        "Your proposed budget (" + amount +
                                ") cannot exceed the project maximum budget (" + budgetMax + ")."
                );
            }
        } catch (NumberFormatException e) {
            // texte non numérique → on laisse passer
        }
    }

    /**
     * Q3 : délai / delivery timeline.
     * - peut être vide
     * - si contient un nombre (en semaines), ce nombre ne doit pas dépasser
     *   durationEstimatedWeeks (si connu).
     */
    private void validateTimeline(String q3, Integer durationEstimatedWeeks) {
        if (q3 == null || q3.trim().isEmpty()) {
            // le freelancer peut laisser vide
            return;
        }

        if (durationEstimatedWeeks == null || durationEstimatedWeeks <= 0) {
            // pas d'info durée côté projet → pas de contrôle
            return;
        }

        String trimmed = q3.trim().toLowerCase();

        // On cherche le PREMIER entier dans la chaîne (ex: "I can do it in 5 weeks")
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\d+")
                .matcher(trimmed);

        if (!matcher.find()) {
            // aucun nombre → on accepte (par ex. "as soon as possible")
            return;
        }

        try {
            int weeks = Integer.parseInt(matcher.group());

            if (weeks > durationEstimatedWeeks) {
                throw new IllegalArgumentException(
                        "Your delivery timeline (" + weeks +
                                " weeks) cannot exceed the estimated project duration (" +
                                durationEstimatedWeeks + " weeks)."
                );
            }
        } catch (NumberFormatException e) {
            // texte non parsable → on laisse passer
        }
    }

}