package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.application.dto.InspectionDTO;

public interface GetInspectionByIdUseCase {
    InspectionDTO getInspectionById(Long inspectionId);
}
