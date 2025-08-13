package com.app.usochicamochabackend.auth.application.port;

import com.app.usochicamochabackend.auth.application.dto.AuthRequest;
import com.app.usochicamochabackend.auth.application.dto.AuthResponse;

public interface LoginUseCase {
    AuthResponse login(AuthRequest authRequest);
}
