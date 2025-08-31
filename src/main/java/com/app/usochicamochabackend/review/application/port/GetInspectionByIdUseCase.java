package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.application.dto.InspectionResponse;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;

public interface GetInspectionByIdUseCase {
    InspectionResponse getInspectionById(Long inspectionId);
}
