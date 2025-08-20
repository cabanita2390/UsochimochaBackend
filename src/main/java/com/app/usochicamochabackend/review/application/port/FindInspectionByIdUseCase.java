package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;

public interface FindInspectionByIdUseCase {
    InspectionEntity findInspectionById(Long id);
}
