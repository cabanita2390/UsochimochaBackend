package com.app.usochicamochabackend.inspection.application.port;

import com.app.usochicamochabackend.inspection.infrastructure.entity.InspectionEntity;

public interface CreateInspectionUseCase {
    InspectionEntity createInspection(InspectionEntity inspectionEntity);
}