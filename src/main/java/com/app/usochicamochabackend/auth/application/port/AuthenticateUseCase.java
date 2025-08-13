package com.app.usochicamochabackend.auth.application.port;

import com.app.usochicamochabackend.auth.application.dto.AuthResponse;
import org.springframework.security.core.Authentication;

public interface AuthenticateUseCase {
    Authentication authenticate(String username, String password);
}
