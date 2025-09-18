package com.app.usochicamochabackend.auth.application.service;

import com.app.usochicamochabackend.auth.application.dto.AuthRequest;
import com.app.usochicamochabackend.auth.application.dto.AuthResponse;
import com.app.usochicamochabackend.auth.application.dto.RefreshTokenRequest;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.auth.utils.JwtUtils;
import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImpTest {

    @Mock
    private UserRepositoryJpa userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserDetailsServiceImp userDetailsService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
    }

    @Test
    void searchUserDetails_ShouldReturnUserDetails_WhenUserExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.searchUserDetails("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("$2a$10$encoded.password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled()); // User constructor sets enabled=true by default
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void searchUserDetails_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class,
            () -> userDetailsService.searchUserDetails("nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Given
        AuthRequest authRequest = new AuthRequest("testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "$2a$10$encoded.password")).thenReturn(true);
        when(jwtUtils.createToken(any())).thenReturn("jwt-token");
        when(jwtUtils.createRefreshToken(any())).thenReturn("refresh-token");

        // When
        AuthResponse response = userDetailsService.login(authRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.jwt());
        assertEquals("refresh-token", response.refreshToken());
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
    }

    @Test
    void login_ShouldThrowException_WhenCredentialsAreInvalid() {
        // Given
        AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encoded.password")).thenReturn(false);

        // When & Then
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> userDetailsService.login(authRequest));
        verify(userRepository, times(2)).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", "$2a$10$encoded.password");
    }

    @Test
    void refreshToken_ShouldReturnNewToken_WhenRefreshTokenIsValid() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        when(jwtUtils.verifyToken("valid-refresh-token")).thenReturn(null); // Mock DecodedJWT
        when(jwtUtils.extractUsername(any())).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtils.createToken(any())).thenReturn("new-access-token");

        // When
        var response = userDetailsService.refreshToken(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.accessToken());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void login_ShouldThrowException_WhenUserIsInactive() {
        // Given
        UserEntity inactiveUser = UserEntity.builder()
                .id(1L)
                .fullName("Test User")
                .username("testuser")
                .email("test@example.com")
                .role("ADMIN")
                .password("$2a$10$encoded.password")
                .status(false) // Inactive
                .build();

        AuthRequest authRequest = new AuthRequest("testuser", "password");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> userDetailsService.login(authRequest));
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void refreshToken_ShouldThrowException_WhenUserIsInactive() {
        // Given
        UserEntity inactiveUser = UserEntity.builder()
                .id(1L)
                .fullName("Test User")
                .username("testuser")
                .email("test@example.com")
                .role("ADMIN")
                .password("$2a$10$encoded.password")
                .status(false) // Inactive
                .build();

        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        when(jwtUtils.verifyToken("valid-refresh-token")).thenReturn(null); // Mock DecodedJWT
        when(jwtUtils.extractUsername(any())).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> userDetailsService.refreshToken(request));
        verify(userRepository).findByUsername("testuser");
    }

}
