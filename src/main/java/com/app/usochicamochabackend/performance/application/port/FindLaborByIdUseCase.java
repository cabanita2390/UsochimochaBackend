package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;

public interface FindLaborByIdUseCase {
    LaborEntity findLaborById(Long id);
}