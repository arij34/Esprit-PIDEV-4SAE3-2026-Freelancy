package com.smartfreelance.backend.controller.dto;

public record AdminCreateUserRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        String role
) {
}
