package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;

import java.util.List;

public interface FindAllInspectionsUseCase {
    List<InspectionEntity> findAllInspections();
}
