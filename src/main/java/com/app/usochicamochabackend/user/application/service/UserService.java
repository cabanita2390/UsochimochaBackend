package com.app.usochicamochabackend.user.application.service;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.user.application.dto.CreateUserRequest;
import com.app.usochicamochabackend.user.application.dto.CreateUserResponse;
import com.app.usochicamochabackend.user.application.port.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements
        CreateUserUseCase,
        DeleteUserUseCase,
        FindAllUsersUseCase,
        FindUserByIdUseCase,
        UpdateUserUseCase {

    private final UserRepositoryJpa userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CreateUserResponse createUser(CreateUserRequest request) {
        UserEntity user = UserEntity.builder()
                .username(request.username())
                .fullName(request.fullName())
                .status(true)
                .email(request.email())
                .role(request.role())
                .password(passwordEncoder.encode(request.password()))
                .build();

        UserEntity userSaved = userRepository.save(user);

        return new CreateUserResponse(user.getId(), userSaved.getUsername(), userSaved.getEmail(),userSaved.getStatus(), userSaved.getRole(), userSaved.getFullName(), "User created successfully");
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserEntity findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public UserEntity updateUser(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }
}