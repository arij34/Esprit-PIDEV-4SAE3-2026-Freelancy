package com.smartfreelance.backend.controller;

import com.smartfreelance.backend.model.User;
import com.smartfreelance.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        System.out.println("Register Request Received: " + user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "Error: Email is already in use!"));
        }

        // Hash password before saving (BCrypt)
        String rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));

        userRepository.save(user);

        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());

        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // Return User object (including Role)
            return ResponseEntity.ok(user);
        }

        return ResponseEntity.status(401).body(java.util.Collections.singletonMap("error", "Invalid email or password"));
    }
}
