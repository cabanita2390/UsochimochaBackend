package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.SparePartEntity;

public interface FindSparePartByIdUseCase {
    SparePartEntity findSparePartById(Long id);
}