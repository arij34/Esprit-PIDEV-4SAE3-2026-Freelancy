package tn.freelancy.skillmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.freelancy.skillmanagement.entity.Experience;
import tn.freelancy.skillmanagement.repository.ExperienceRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ExperienceService {

    @Autowired
    private ExperienceRepository experienceRepository;

    // ✅ SUPPRIMÉ : UserRepository userRepository (n'existe plus)

    public Experience createExperience(Long userId, Experience experience) {
        // ✅ CORRIGÉ : on stocke directement le userId
        experience.setUserId(userId);
        return experienceRepository.save(experience);
    }

    public List<Experience> getAllExperiences() {
        return experienceRepository.findAll();
    }

    public Experience getExperienceById(Long id) {
        return experienceRepository.findById(id).orElse(null);
    }

    // ✅ AJOUTÉ : toutes les expériences d'un utilisateur (appelé par GET /user/me)
    public List<Experience> getExperiencesByUserId(Long userId) {
        return experienceRepository.findByUserId(userId);
    }

    public Experience updateExperience(Experience updatedExperience) {
        return experienceRepository.save(updatedExperience);
    }

    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }

    public double calculateTotalYearsByUser(Long userId) {
        // ✅ CORRIGÉ : findByUserId au lieu de findByUser_Id (plus de relation JPA)
        List<Experience> experiences = experienceRepository.findByUserId(userId);

        long totalMonths = 0;
        for (Experience exp : experiences) {
            if (exp.getStartDate() != null) {
                LocalDate start = exp.getStartDate();
                LocalDate end = exp.getEndDate() != null
                        ? exp.getEndDate()
                        : LocalDate.now();
                totalMonths += ChronoUnit.MONTHS.between(start, end);
            }
        }
        return Math.round((totalMonths / 12.0) * 10.0) / 10.0;
    }
}