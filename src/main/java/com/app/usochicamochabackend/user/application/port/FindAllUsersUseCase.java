package com.app.usochicamochabackend.user.application.port;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;

import java.util.List;

public interface FindAllUsersUseCase {
    List<UserEntity> findAllUsers();
}
