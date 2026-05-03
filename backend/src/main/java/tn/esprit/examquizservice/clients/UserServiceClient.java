package tn.esprit.examquizservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "smart-freelance-backend")
public interface UserServiceClient {

    @GetMapping("/api/public/ping")
    Map<String, String> ping();

    @GetMapping("/api/me")
    UserDto getCurrentUser(@RequestHeader("Authorization") String bearerToken);

    @GetMapping("/api/admin/users")
    List<Map<String, Object>> getAllUsers(@RequestHeader("Authorization") String bearerToken);

    /**
     * Adds {@code points} to the experiencePoints field of the given user.
     * Adjust the path to match the actual endpoint exposed by the user service.
     */
    @PutMapping("/api/users/{userId}/add-experience")
    void addExperiencePoints(@PathVariable("userId") Long userId,
                             @RequestParam("points") Double points);
}
