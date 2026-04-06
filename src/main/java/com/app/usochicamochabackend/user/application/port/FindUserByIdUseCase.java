package com.app.usochicamochabackend.user.application.port;

import com.app.usochicamochabackend.user.application.dto.UserResponse;

public interface FindUserByIdUseCase {
    UserResponse findUserById(Long id);
}
