package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.SparePartEntity;
import java.util.List;

public interface FindAllSparePartsUseCase {
    List<SparePartEntity> findAllSpareParts();
}