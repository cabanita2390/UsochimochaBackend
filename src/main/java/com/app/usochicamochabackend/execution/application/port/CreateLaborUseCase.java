package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.LaborEntity;

public interface CreateLaborUseCase {
    LaborEntity createLabor(LaborEntity laborEntity);
}