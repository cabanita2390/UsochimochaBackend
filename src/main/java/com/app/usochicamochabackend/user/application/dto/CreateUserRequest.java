package com.app.usochicamochabackend.user.application.dto;

import com.app.usochicamochabackend.auth.domain.enums.RoleEnum;

public record CreateUserRequest(String username, String fullName, String role, String email, String password) {
}
