package tn.freelancy.skillmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.freelancy.skillmanagement.entity.Experience;
import tn.freelancy.skillmanagement.entity.User;
import tn.freelancy.skillmanagement.repository.ExperienceRepository;
import tn.freelancy.skillmanagement.repository.UserRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ExperienceService {

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private UserRepository userRepository;

    public Experience createExperience(Long userId, Experience experience) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        experience.setUser(user);

        return experienceRepository.save(experience);
    }

    public List<Experience> getAllExperiences() {
        return experienceRepository.findAll();
    }

    public Experience getExperienceById(Long id) {
        return experienceRepository.findById(id).orElse(null);
    }

    public Experience updateExperience(Experience updatedExperience) {
        return experienceRepository.save(updatedExperience);
    }

    public void deleteExperience(Long id) {
        experienceRepository.deleteById(id);
    }
    public double calculateTotalYearsByUser(Long userId) {
        List<Experience> experiences = experienceRepository.findByUser_Id(userId);

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

