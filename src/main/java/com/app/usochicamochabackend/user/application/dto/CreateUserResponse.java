package com.app.usochicamochabackend.user.application.dto;

public record CreateUserResponse(Long id, String username, String email, boolean status,  String role , String fullName,String message) {}