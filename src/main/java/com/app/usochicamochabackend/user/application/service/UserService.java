package com.app.usochicamochabackend.user.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.mapper.UserMapper;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.user.application.dto.*;
import com.app.usochicamochabackend.user.application.port.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements
        CreateUserUseCase,
        DeleteUserUseCase,
        FindAllUsersUseCase,
        FindUserByIdUseCase,
        UpdateUserUseCase,
        ChangePasswordUseCase {

    private final UserRepositoryJpa userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SaveActionUseCase saveActionUseCase;
    private final NotificationService notificationService;

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

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        saveActionUseCase.save("El usuario " + userSaved.getUsername() + " ha sido creado por " + userPrincipal.username());
        notificationService.notify("users-updated");
        notificationService.notify("actions-updated");

        return new CreateUserResponse(user.getId(), userSaved.getUsername(), userSaved.getEmail(),userSaved.getStatus(), userSaved.getRole(), userSaved.getFullName(), "User created successfully");
    }

    @Override
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setStatus(false);

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        saveActionUseCase.save("El usuario " + user.getUsername() + " ha sido eliminado por " + userPrincipal.username());
        notificationService.notify("users-updated");
        notificationService.notify("actions-updated");

        userRepository.save(user);
    }

    @Override
    public UsersResponse findAllUsers() {
        List<UserEntity> userEntities = userRepository.findAll()
                .stream()
                .filter(UserEntity::getStatus)
                .toList();

        notificationService.notify("actions-updated");

        return UserMapper.toResponse(userEntities);
    }

    @Override
    public UserResponse findUserById(Long id) {
         UserEntity user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found with id: " + id));

        notificationService.notify("actions-updated");

         return UserMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUser(UpdateUserRequest request) throws ResourceNotFoundException, URISyntaxException {
        UserEntity currentUser = userRepository.getUserEntityById(request.id());

        List<String> changes = new ArrayList<>();

        Optional.ofNullable(request.username()).ifPresent(username -> {
            currentUser.setUsername(username);
            changes.add("username");
        });

        Optional.ofNullable(request.fullName()).ifPresent(fullName -> {
            currentUser.setFullName(fullName);
            changes.add("fullName");
        });

        Optional.ofNullable(request.email()).ifPresent(email -> {
            currentUser.setEmail(email);
            changes.add("email");
        });

        Optional.ofNullable(request.role()).ifPresent(role -> {
            currentUser.setRole(role);
            changes.add("role");
        });

        UserEntity userUpdated = userRepository.save(currentUser);

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String mensaje = "El usuario " + userUpdated.getUsername() +
                " ha sido actualizado por " + userPrincipal.username();

        if (!changes.isEmpty()) {
            mensaje += ". Campos modificados: " + String.join(", ", changes);
        }

        saveActionUseCase.save(mensaje);

        notificationService.notify("actions-updated");
        notificationService.notify("users-updated");

        return UserMapper.toResponse(userUpdated);
    }


    @Override
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
        UserEntity currentUser = userRepository.findById(request.id()).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.id()));

        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));
        UserResponse userUpdated = UserMapper.toResponse(userRepository.save(currentUser));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        saveActionUseCase.save("La contraseña del " + currentUser.getUsername() + " ha sido actualizada por " + userPrincipal.username());

        notificationService.notify("actions-updated");
        notificationService.notify("users-updated");

        return new ChangePasswordResponse(userUpdated, "Password was change successfully", true);
    }
}