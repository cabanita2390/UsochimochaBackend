package com.app.usochicamochabackend.user.application.port;

import com.app.usochicamochabackend.user.application.dto.ChangePasswordRequest;
import com.app.usochicamochabackend.user.application.dto.ChangePasswordResponse;

public interface ChangePasswordUseCase {
    ChangePasswordResponse changePassword(ChangePasswordRequest changePasswordRequest);
}
