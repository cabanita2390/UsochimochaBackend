package com.app.usochicamochabackend.user.application.dto;

public record ChangePasswordResponse(UserResponse user, String message, Boolean status) {}