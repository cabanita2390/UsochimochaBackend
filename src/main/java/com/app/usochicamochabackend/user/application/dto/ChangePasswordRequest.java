package com.app.usochicamochabackend.user.application.dto;

public record ChangePasswordRequest(Long id, String newPassword) {}