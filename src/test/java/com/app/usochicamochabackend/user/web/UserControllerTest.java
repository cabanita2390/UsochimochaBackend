package com.app.usochicamochabackend.user.web;

import com.app.usochicamochabackend.user.application.dto.*;
import com.app.usochicamochabackend.user.application.port.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateUserUseCase createUserUseCase;

    @MockBean
    private DeleteUserUseCase deleteUserUseCase;

    @MockBean
    private FindAllUsersUseCase findAllUsersUseCase;

    @MockBean
    private FindUserByIdUseCase findUserByIdUseCase;

    @MockBean
    private UpdateUserUseCase updateUserUseCase;

    @MockBean
    private ChangePasswordUseCase changePasswordUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("New User", "newuser", "new@example.com", "MECHANIC", "password");
        CreateUserResponse response = new CreateUserResponse(1L, "New User", "newuser", "new@example.com", "MECHANIC", true, "User created successfully");
        when(createUserUseCase.createUser(any(CreateUserRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/user")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("New User"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.role").value("MECHANIC"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        // Given
        List<UserResponse> users = Arrays.asList(
                new UserResponse(1L, "user1", "User 1", "user1@example.com", "ADMIN"),
                new UserResponse(2L, "user2", "User 2", "user2@example.com", "MECHANIC")
        );
        UsersResponse response = new UsersResponse(users);
        when(findAllUsersUseCase.findAllUsers()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/user")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users.length()").value(2))
                .andExpect(jsonPath("$.users[0].fullName").value("User 1"))
                .andExpect(jsonPath("$.users[1].fullName").value("User 2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturnUser() throws Exception {
        // Given
        UserResponse response = new UserResponse(1L, "testuser", "Test User", "test@example.com", "ADMIN");
        when(findUserByIdUseCase.findUserById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/user/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(1L, "Updated User", true, "updateduser", "updated@example.com", "ADMIN");
        UserResponse response = new UserResponse(1L, "updateduser", "Updated User", "updated@example.com", "ADMIN");
        when(updateUserUseCase.updateUser(any(UpdateUserRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/user/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated User"))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/user/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changePassword_ShouldReturnSuccessMessage() throws Exception {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(1L, "newpassword");
        UserResponse userResponse = new UserResponse(1L, "Test User", "testuser", "test@example.com", "ADMIN");
        ChangePasswordResponse response = new ChangePasswordResponse(userResponse, "Password was change successfully", true);
        when(changePasswordUseCase.changePassword(any(ChangePasswordRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/user/1/change-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password was change successfully"))
                .andExpect(jsonPath("$.status").value(true));
    }
}
