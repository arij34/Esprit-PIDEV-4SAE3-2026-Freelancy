package com.smartfreelance.backend.controller;

import com.smartfreelance.backend.model.Role;
import com.smartfreelance.backend.model.User;
import com.smartfreelance.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        String roleName = payload.get("role");
        try {
            Role role = Role.valueOf(roleName);
            user.setRole(role);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Invalid role"));
        }
    }
}
