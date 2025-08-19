package com.app.usochicamochabackend.user.application.port;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;

public interface UpdateUserUseCase {
    UserEntity updateUser(UserEntity userEntity);
}
