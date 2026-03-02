package tn.freelancy.skillmanagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.freelancy.skillmanagement.entity.Education;
import tn.freelancy.skillmanagement.entity.User;
import tn.freelancy.skillmanagement.repository.EducationRepository;
import tn.freelancy.skillmanagement.repository.UserRepository;

import java.util.List;

@Service
public class EducationService {

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private UserRepository userRepository;

    public Education createEducation(Long userId, Education education) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        education.setUser(user);

        return educationRepository.save(education);
    }

    public List<Education> getAllEducations() {
        return educationRepository.findAll();
    }

    public Education getEducationById(Long id) {
        return educationRepository.findById(id).orElse(null);
    }

    public Education updateEducation(Education updatedEducation) {
        return educationRepository.save(updatedEducation);
    }

    public void deleteEducation(Long id) {
        educationRepository.deleteById(id);
    }
    public Education getLatestEducation(Long userId) {
        return educationRepository.findTopByUser_IdOrderByYearDesc(userId);
    }
}
