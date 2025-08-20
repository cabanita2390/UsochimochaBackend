package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;

public interface FindSparePartByIdUseCase {
    SparePartEntity findSparePartById(Long id);
}