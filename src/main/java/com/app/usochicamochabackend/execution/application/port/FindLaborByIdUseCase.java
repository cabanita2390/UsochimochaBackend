package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.LaborEntity;

public interface FindLaborByIdUseCase {
    LaborEntity findLaborById(Long id);
}