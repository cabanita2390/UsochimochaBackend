package com.app.usochicamochabackend.inspection.application.port;

import com.app.usochicamochabackend.inspection.infrastructure.entity.InspectionEntity;

import java.util.List;

public interface FindAllInspectionsUseCase {
    List<InspectionEntity> findAllInspections();
}
