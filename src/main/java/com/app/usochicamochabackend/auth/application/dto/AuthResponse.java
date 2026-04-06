package com.app.usochicamochabackend.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "username", "fullName", "role", "message", "jwt", "refreshToken", "status" })
public record AuthResponse(Long id, String username, String fullName, String role, String message, String jwt,
        String refreshToken, boolean status) {
}
