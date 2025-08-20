package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;

public interface UpdateSparePartUseCase {
    SparePartEntity updateSparePart(SparePartEntity sparePartEntity);
}