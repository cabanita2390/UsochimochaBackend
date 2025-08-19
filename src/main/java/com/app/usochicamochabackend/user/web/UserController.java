package com.app.usochicamochabackend.user.web;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.user.application.dto.CreateUserRequest;
import com.app.usochicamochabackend.user.application.dto.CreateUserResponse;
import com.app.usochicamochabackend.user.application.port.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final FindAllUsersUseCase findAllUsersUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;

    /* --- READ --- */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(findUserByIdUseCase.findUserById(id));
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserEntity>> getUsers() {
        return ResponseEntity.ok(findAllUsersUseCase.findAllUsers());
    }

    /* --- CREATE --- */
    @PostMapping
    @Operation(summary = "Create user")
    @ApiResponse(responseCode = "201", description = "User created")
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest request)
            throws URISyntaxException {
        CreateUserResponse saved = createUserUseCase.createUser(request);
        return ResponseEntity
                .created(new URI("/api/v1/user/" + saved.id()))
                .body(saved);
    }

    /* --- UPDATE --- */
    @PutMapping("/{id}")
    @Operation(summary = "Update User")
    public ResponseEntity<UserEntity> updateUser(
            @PathVariable Long id,
            @RequestBody UserEntity userEntity) throws URISyntaxException {

        userEntity.setId(id);
        UserEntity updated = updateUserUseCase.updateUser(userEntity);
        return ResponseEntity
                .created(new URI("/api/v1/user/" + updated.getId()))
                .body(updated);
    }

    /* --- DELETE --- */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete User")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        deleteUserUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}