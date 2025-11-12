package com.app.usochicamochabackend.user.application.service;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.user.application.dto.*;
import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.utils.TestDataBuilder;
import com.app.usochicamochabackend.utils.TestSecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryJpa userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        TestSecurityUtils.setUpSecurityContext(1L, "testuser", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void createUser_ShouldReturnCreateUserResponse_WhenUserIsCreated() {
        // Given
        CreateUserRequest request = new CreateUserRequest("New User", "newuser", "new@example.com", "MECHANIC", "password");
        UserEntity savedUser = UserEntity.builder()
                .id(2L)
                .fullName("New User")
                .username("newuser")
                .email("new@example.com")
                .role("MECHANIC")
                .password("encoded-password")
                .status(true)
                .build();

        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        CreateUserResponse response = userService.createUser(request);

        // Then
        assertNotNull(response);
        assertEquals("New User", response.fullName());
        assertEquals("newuser", response.username());
        assertEquals("new@example.com", response.email());
        assertEquals("MECHANIC", response.role());
        assertTrue(response.status());

        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(UserEntity.class));
        verify(saveActionUseCase).save(anyString());
    }

    @Test
    void findAllUsers_ShouldReturnUsersResponse() {
        // Given
        UserEntity user2 = TestDataBuilder.createTestMechanic();
        List<UserEntity> users = Arrays.asList(testUser, user2);
        when(userRepository.findAll()).thenReturn(users);

        // When
        UsersResponse response = userService.findAllUsers();

        // Then
        assertNotNull(response);
        assertEquals(2, response.users().size());
        verify(userRepository).findAll();
    }

    @Test
    void findUserById_ShouldReturnUserResponse_WhenUserExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserResponse response = userService.findUserById(1L);

        // Then
        assertNotNull(response);
        assertEquals("Test User", response.fullName());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        verify(userRepository).findById(1L);
    }

    @Test
    void findUserById_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.findUserById(999L));
        verify(userRepository).findById(999L);
    }

    @Test
    void updateUser_ShouldReturnUserResponse_WhenUserIsUpdated() throws URISyntaxException {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(1L, "Updated User", true, "updateduser", "updated@example.com", "ADMIN");
        UserEntity updatedUser = UserEntity.builder()
                .id(1L)
                .fullName("Updated User")
                .username("updateduser")
                .email("updated@example.com")
                .role("ADMIN")
                .password("$2a$10$encoded.password")
                .status(true)
                .build();

        when(userRepository.getUserEntityById(1L)).thenReturn(testUser);
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedUser);

        // When
        UserResponse response = userService.updateUser(request);

        // Then
        assertNotNull(response);
        assertEquals("Updated User", response.fullName());
        assertEquals("updateduser", response.username());
        assertEquals("updated@example.com", response.email());
        assertEquals("ADMIN", response.role());

        verify(userRepository).getUserEntityById(1L);
        verify(userRepository).save(any(UserEntity.class));
        verify(saveActionUseCase).save(anyString());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
        verify(saveActionUseCase).save(anyString());
    }

    @Test
    void changePassword_ShouldReturnChangePasswordResponse_WhenPasswordIsChanged() {
        // Given
        ChangePasswordRequest request = new ChangePasswordRequest(1L, "newpassword");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("new-encoded-password");

        // When
        ChangePasswordResponse response = userService.changePassword(request);

        // Then
        assertNotNull(response);
        assertEquals("Password was change successfully", response.message());

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(any(UserEntity.class));
        verify(saveActionUseCase).save(anyString());
    }


}
