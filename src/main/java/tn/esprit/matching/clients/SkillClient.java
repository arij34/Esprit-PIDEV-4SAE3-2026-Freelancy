package tn.esprit.matching.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "skill-management")
public interface SkillClient {

    @GetMapping("/experience/user/{userId}/matching")
    ExperienceMatchingResponse getExperiences(@PathVariable Long userId);

    @GetMapping("/availability/user/{userId}/matching")
    AvailabilityDTO getAvailability(@PathVariable Long userId);

    @GetMapping("/education/user/{userId}/matching")
    EducationMatchingResponse getEducation(@PathVariable Long userId);

    @GetMapping("/freelancer-skill/user/{userId}/matching")
    List<FreelancerSkillMatchingResponse> getFreelancerSkills(@PathVariable Long userId);

    @GetMapping("/api/user-service/freelancers")
    List<UserDto> getAllUsers(@RequestHeader("Authorization") String token);
}