package com.app.usochicamochabackend.actions.application.port;

import com.app.usochicamochabackend.actions.infrastructure.entity.ActionEntity;

public interface SaveActionUseCase {
    void save(String details);
}
