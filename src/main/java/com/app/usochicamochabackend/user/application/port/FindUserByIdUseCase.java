package com.app.usochicamochabackend.user.application.port;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;

public interface FindUserByIdUseCase {
    UserEntity findUserById(Long id);
}
