package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;
import java.util.List;

public interface FindAllSparePartsUseCase {
    List<SparePartEntity> findAllSpareParts();
}