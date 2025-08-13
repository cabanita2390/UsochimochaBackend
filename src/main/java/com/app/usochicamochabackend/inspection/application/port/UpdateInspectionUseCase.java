package com.app.usochicamochabackend.inspection.application.port;

import com.app.usochicamochabackend.inspection.infrastructure.entity.InspectionEntity;

public interface UpdateInspectionUseCase {
    InspectionEntity updateInspection(InspectionEntity inspectionEntity);
}
