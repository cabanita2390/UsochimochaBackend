package com.app.usochicamochabackend.user.application.dto;

public record UserResponse(Long id, String username, String fullName, String email, String role) {
}
