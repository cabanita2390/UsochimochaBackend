package com.app.usochicamochabackend.user.application.dto;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;

import java.util.List;

public record UsersResponse(List<UserResponse> users) {
}
