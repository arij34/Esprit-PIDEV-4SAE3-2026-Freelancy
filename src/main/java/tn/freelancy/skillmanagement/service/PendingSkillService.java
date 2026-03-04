package tn.freelancy.skillmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.freelancy.skillmanagement.entity.*;
import tn.freelancy.skillmanagement.repository.*;

import java.util.Date;
import java.util.List;

@Service
public class PendingSkillService {

    @Autowired
    private PendingSkillRepository pendingSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private FreelancerSkillRepository freelancerSkillRepository;

    // ✅ AJOUT : injection du service de notification
    @Autowired
    private NotificationService notificationService;

    // ══════════════════════════════════════════════════════════════
    // GET ALL DRAFTS
    // ══════════════════════════════════════════════════════════════

    public List<PendingSkill> getAllDrafts() {
        return pendingSkillRepository.findByStatus(Status.DRAFT);
    }

    // ══════════════════════════════════════════════════════════════
    // APPROVE
    // ══════════════════════════════════════════════════════════════

    public void approvePendingSkill(Long pendingId) {

        PendingSkill pending = pendingSkillRepository
                .findById(pendingId)
                .orElseThrow();

        // Vérifier si le skill existe déjà dans la table Skill
        Skill existing = skillRepository
                .findByNormalizedNameIgnoreCase(pending.getNormalizedName());

        if (existing == null) {
            Skill newSkill = new Skill();
            newSkill.setName(pending.getSuggestedName());
            newSkill.setNormalizedName(pending.getNormalizedName());
            newSkill.setCreatedAt(new Date());
            existing = skillRepository.save(newSkill);
        }

        pending.setStatus(Status.APPROVED);
        pendingSkillRepository.save(pending);

        // Mettre à jour les FreelancerSkill qui utilisaient ce customSkillName
        List<FreelancerSkill> freelancerSkills =
                freelancerSkillRepository.findByCustomSkillName(
                        pending.getSuggestedName()
                );

        for (FreelancerSkill fs : freelancerSkills) {
            fs.setSkill(existing);
            freelancerSkillRepository.save(fs);
        }

        // ✅ AJOUT : notifier le freelancer que son skill est approuvé
        if (pending.getSuggestedBy() != null) {
            notificationService.notifyFreelancerSkillApproved(
                    pending.getSuggestedName(),
                    pending.getSuggestedBy(),
                    "User #" + pending.getSuggestedBy()   // remplacez par le vrai nom si disponible
            );
        }

        // Supprimer le pending
        pendingSkillRepository.delete(pending);
    }

    // ══════════════════════════════════════════════════════════════
    // REJECT
    // ══════════════════════════════════════════════════════════════

    public void reject(Long id) {

        PendingSkill pending = pendingSkillRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        pending.setStatus(Status.REJECTED);
        pendingSkillRepository.save(pending);

        // ✅ AJOUT : notifier le freelancer que son skill est rejeté
        if (pending.getSuggestedBy() != null) {
            notificationService.notifyFreelancerSkillRejected(
                    pending.getSuggestedName(),
                    pending.getSuggestedBy(),
                    "User #" + pending.getSuggestedBy()   // remplacez par le vrai nom si disponible
            );
        }
    }

    // ══════════════════════════════════════════════════════════════
    // CREATE PENDING SKILL
    // ══════════════════════════════════════════════════════════════

    public void createPendingSkill(String skillInput, User user, Source source) {

        String normalized = skillInput.toLowerCase().trim();

        boolean exists = pendingSkillRepository
                .existsByNormalizedNameAndStatus(normalized, Status.DRAFT);

        if (!exists) {

            PendingSkill pending = new PendingSkill();
            pending.setSuggestedName(skillInput);
            pending.setNormalizedName(normalized);
            pending.setSuggestedBy(user.getId());
            pending.setSource(Source.FREELANCER);
            pending.setStatus(Status.DRAFT);

            pendingSkillRepository.save(pending);

            // ✅ AJOUT : notifier l'admin en temps réel via WebSocket
            String freelancerName = (user.getNom() != null)
                    ? user.getNom() + " " + user.getNom()
                    : "User #" + user.getId();

            notificationService.notifyAdminNewPendingSkill(
                    skillInput,
                    user.getId(),
                    freelancerName
            );
        }
    }
}