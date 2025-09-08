package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;

public interface CreateInspectionOnlyDataUseCase {
    InspectionFormResponse createInspectionOnlyData(InspectionFormRequest inspectionFormRequest);
}
