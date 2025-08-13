package com.app.usochicamochabackend.auth.application.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"username", "message", "jwt", "refreshToken", "status"})
public record AuthResponse(String username, String message, String jwt, String refreshToken, boolean status) {}
