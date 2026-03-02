package tn.freelancy.skillmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.freelancy.skillmanagement.dto.SkillMatchResult;
import tn.freelancy.skillmanagement.entity.*;
import tn.freelancy.skillmanagement.repository.FreelancerSkillRepository;
import tn.freelancy.skillmanagement.repository.PendingSkillRepository;
import tn.freelancy.skillmanagement.repository.SkillRepository;
import tn.freelancy.skillmanagement.repository.UserRepository;

import java.util.List;

@Service
public class FreelancerSkillService {

    @Autowired
    private FreelancerSkillRepository freelancerSkillRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillMatcherService skillMatcherService;

    @Autowired
    private PendingSkillService pendingSkillService;

    /* =========================
       GET METHODS
    ========================== */

    public List<FreelancerSkill> getAllFreelancerSkills() {
        return freelancerSkillRepository.findAll();
    }

    public FreelancerSkill getFreelancerSkillById(Long id) {
        return freelancerSkillRepository.findById(id).orElse(null);
    }

    public void deleteFreelancerSkill(Long id) {
        freelancerSkillRepository.deleteById(id);
    }

    /* =========================
       LEVEL CALCULATION
    ========================== */

    public level calculateLevel(int yearsExperience) {
        if (yearsExperience == 0) return level.BEGINNER;
        else if (yearsExperience <= 2) return level.ELEMENTARY;
        else if (yearsExperience <= 4) return level.INTERMEDIATE;
        else if (yearsExperience <= 7) return level.ADVANCED;
        else return level.EXPERT;
    }

    /* =========================
       CREATE
    ========================== */

    public FreelancerSkill createFreelancerSkill(Long userId,
                                                 FreelancerSkill freelancerSkill,
                                                 String skillInput) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        freelancerSkill.setUser(user);
        freelancerSkill.setLevel(
                calculateLevel(freelancerSkill.getYearsExperience())
        );

        skillInput = skillInput.trim();

        SkillMatchResult result = skillMatcherService.findMatchingSkill(skillInput);

        if (result != null && result.getSkill() != null) {

            double confidence = result.getConfidence();

            if (confidence > 0) {

                freelancerSkill.setSkill(result.getSkill());
                freelancerSkill.setCustomSkillName(skillInput);

            } else {

                pendingSkillService.createPendingSkill(skillInput,user, Source.FREELANCER);

                freelancerSkill.setSkill(null);
                freelancerSkill.setCustomSkillName(skillInput);
            }

        } else {

            pendingSkillService.createPendingSkill(skillInput, user, Source.FREELANCER);
            freelancerSkill.setSkill(null);
            freelancerSkill.setCustomSkillName(skillInput);
        }

        return freelancerSkillRepository.save(freelancerSkill);
    }

    /* =========================
       UPDATE
    ========================== */

    public FreelancerSkill updateFreelancerSkill(FreelancerSkill freelancerSkill) {

        if (freelancerSkill.getSkill() != null
                && freelancerSkill.getSkill().getIdS() != null) {

            Skill skill = skillRepository.findById(
                            freelancerSkill.getSkill().getIdS())
                    .orElseThrow(() -> new RuntimeException("Skill not found"));

            freelancerSkill.setSkill(skill);
        }

        freelancerSkill.setLevel(
                calculateLevel(freelancerSkill.getYearsExperience())
        );

        return freelancerSkillRepository.save(freelancerSkill);
    }
}