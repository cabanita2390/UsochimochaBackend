package com.app.usochicamochabackend.config;

import com.app.usochicamochabackend.auth.application.port.LoginUseCase;
import com.app.usochicamochabackend.auth.application.port.RefreshTokenUseCase;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestWebConfig {
    @Bean
    public NotificationService notificationService() {
        return mock(NotificationService.class);
    }

    @Bean
    public LoginUseCase loginUseCase() {
        return mock(LoginUseCase.class);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase() {
        return mock(RefreshTokenUseCase.class);
    }
}