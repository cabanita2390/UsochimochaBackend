package com.app.usochicamochabackend.auth.application.port;

import com.app.usochicamochabackend.auth.application.dto.RefreshTokenRequest;
import com.app.usochicamochabackend.auth.application.dto.RefreshTokenResponse;

public interface RefreshTokenUseCase {
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
}
