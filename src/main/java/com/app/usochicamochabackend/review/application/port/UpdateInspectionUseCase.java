package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;

public interface UpdateInspectionUseCase {
    InspectionEntity updateInspection(InspectionEntity inspectionEntity);
}
