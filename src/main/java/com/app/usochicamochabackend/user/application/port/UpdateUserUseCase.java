package com.app.usochicamochabackend.user.application.port;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.user.application.dto.UpdateUserRequest;
import com.app.usochicamochabackend.user.application.dto.UserResponse;

import java.net.URISyntaxException;

public interface UpdateUserUseCase {
    UserResponse updateUser(UpdateUserRequest user) throws URISyntaxException;
}
