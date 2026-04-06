package com.app.usochicamochabackend.auth.application.port;

import com.app.usochicamochabackend.auth.application.dto.AuthResponse;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import org.springframework.security.core.Authentication;

public interface AuthenticateUseCase {
    Authentication authenticate(UserPrincipal userPrincipal, String password);
}
