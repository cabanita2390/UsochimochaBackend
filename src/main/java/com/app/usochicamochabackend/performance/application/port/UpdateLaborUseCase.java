package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;

public interface UpdateLaborUseCase {
    LaborEntity updateLabor(LaborEntity laborEntity);
}