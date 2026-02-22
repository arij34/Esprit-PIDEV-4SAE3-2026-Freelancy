package com.smartfreelance.backend.controller.dto;

public record AdminUpdateUserRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        Boolean enabled,
        String role
) {
}
