package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;

public interface CreateLaborUseCase {
    LaborEntity createLabor(LaborEntity laborEntity);
}