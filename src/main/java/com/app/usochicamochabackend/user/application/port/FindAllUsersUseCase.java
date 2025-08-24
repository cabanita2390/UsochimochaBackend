package com.app.usochicamochabackend.user.application.port;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.user.application.dto.UsersResponse;

import java.util.List;

public interface FindAllUsersUseCase {
    UsersResponse findAllUsers();
}
