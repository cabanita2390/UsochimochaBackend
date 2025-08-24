package com.app.usochicamochabackend.user.application.dto;

public record UpdateUserRequest(Long id, String fullName, Boolean status, String username,String email, String role) {}