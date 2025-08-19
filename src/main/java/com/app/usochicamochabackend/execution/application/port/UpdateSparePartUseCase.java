package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.SparePartEntity;

public interface UpdateSparePartUseCase {
    SparePartEntity updateSparePart(SparePartEntity sparePartEntity);
}