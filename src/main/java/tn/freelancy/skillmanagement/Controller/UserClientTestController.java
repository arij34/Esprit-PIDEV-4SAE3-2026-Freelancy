package tn.freelancy.skillmanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.freelancy.skillmanagement.clients.UserDto;
import tn.freelancy.skillmanagement.clients.UserServiceClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-service")
public class UserClientTestController {


    @Autowired
    private UserServiceClient userServiceClient;

    public UserClientTestController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    /**
     * Simple endpoint to test communication with the user microservice via Eureka + Feign.
     *
     * In Swagger, call this endpoint with an Authorization header (e.g. "Bearer ...")
     * and it will internally call the remote /api/public/ping on smart-freelance-backend.
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> pingUserService(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Map<String, String> response = userServiceClient.ping();
        return ResponseEntity.ok(response);
    }



    @GetMapping("/freelancers")
    public List<UserDto> getFreelancers(@RequestHeader("Authorization") String token) {
        List<Map<String, Object>> users = userServiceClient.getFreelancers(token);
        return users.stream().map(u -> new UserDto(
                Long.valueOf(u.get("id").toString()),
                (String) u.get("firstName"),
                (String) u.get("lastName"),
                (String) u.get("email"),
                (String) u.get("role"),
                (Boolean) u.get("enabled")
        )).toList();
    }

}

