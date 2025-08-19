package com.app.usochicamochabackend.user.application.port;

import com.app.usochicamochabackend.user.application.dto.CreateUserRequest;
import com.app.usochicamochabackend.user.application.dto.CreateUserResponse;

public interface CreateUserUseCase {
    CreateUserResponse createUser(CreateUserRequest request);
}