package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.SparePartEntity;

public interface CreateSparePartUseCase {
    SparePartEntity createSparePart(SparePartEntity sparePartEntity);
}