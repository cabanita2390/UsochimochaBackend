package com.app.usochicamochabackend.user.application.dto;

public record CreateUserRequest(String username, String fullName, String role, String email, String password) {
}
