package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.LaborEntity;
import java.util.List;

public interface FindAllLaborsUseCase {
    List<LaborEntity> findAllLabors();
}