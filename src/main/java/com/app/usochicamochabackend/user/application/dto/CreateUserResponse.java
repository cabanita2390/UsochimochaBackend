package com.app.usochicamochabackend.user.application.dto;

public record CreateUserResponse(Long id, String fullName, String username, String email, String role, boolean status, String message) {}