package com.app.usochicamochabackend.auth.web;

import com.app.usochicamochabackend.auth.application.dto.AuthRequest;
import com.app.usochicamochabackend.auth.application.dto.AuthResponse;
import com.app.usochicamochabackend.auth.application.dto.RefreshTokenRequest;
import com.app.usochicamochabackend.auth.application.dto.RefreshTokenResponse;
import com.app.usochicamochabackend.auth.application.port.LoginUseCase;
import com.app.usochicamochabackend.auth.application.port.RefreshTokenUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("testuser", "password");
        AuthResponse authResponse = new AuthResponse(1L, "testuser", "Login successful", "jwt-token", "refresh-token", true);
        when(loginUseCase.login(any(AuthRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @WithMockUser
    void login_ShouldReturnBadRequest_WhenCredentialsAreInvalid() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword");
        when(loginUseCase.login(any(AuthRequest.class))).thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void refreshToken_ShouldReturnNewToken_WhenRefreshTokenIsValid() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        RefreshTokenResponse refreshResponse = new RefreshTokenResponse("new-jwt-token");
        when(refreshTokenUseCase.refreshToken(any(RefreshTokenRequest.class))).thenReturn(refreshResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt-token"));
    }
}
