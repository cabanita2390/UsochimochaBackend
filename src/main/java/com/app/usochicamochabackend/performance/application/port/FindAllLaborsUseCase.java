package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import java.util.List;

public interface FindAllLaborsUseCase {
    List<LaborEntity> findAllLabors();
}