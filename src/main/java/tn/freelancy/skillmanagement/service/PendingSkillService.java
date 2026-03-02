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

    public List<PendingSkill> getAllDrafts() {
        return pendingSkillRepository.findByStatus(Status.DRAFT);
    }

    @Autowired
    private FreelancerSkillRepository freelancerSkillRepository;

    public void approvePendingSkill(Long pendingId) {

        PendingSkill pending = pendingSkillRepository
                .findById(pendingId)
                .orElseThrow();

        // Vérifier si le skill existe déjà
        Skill existing = skillRepository
                .findByNormalizedNameIgnoreCase(pending.getNormalizedName());

        if (existing == null) {

            Skill newSkill = new Skill();
            newSkill.setName(pending.getSuggestedName());
            newSkill.setNormalizedName(pending.getNormalizedName());
            newSkill.setCreatedAt(new Date());

            skillRepository.save(newSkill);
        }

        pending.setStatus(Status.APPROVED);
        pendingSkillRepository.save(pending);


        // 2️⃣ Update freelancer_skill where customSkillName = suggestedName
        List<FreelancerSkill> freelancerSkills =
                freelancerSkillRepository.findByCustomSkillName(
                        pending.getSuggestedName()
                );

        for (FreelancerSkill fs : freelancerSkills) {
            fs.setSkill(existing);
            freelancerSkillRepository.save(fs);
        }

        // 3️⃣ Delete pending
        pendingSkillRepository.delete(pending);
    }

    public void reject(Long id) {

        PendingSkill pending = pendingSkillRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        pending.setStatus(Status.REJECTED);
        pendingSkillRepository.save(pending);

    }

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
        }
    }
}