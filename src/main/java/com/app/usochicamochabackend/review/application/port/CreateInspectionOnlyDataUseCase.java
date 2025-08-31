package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.dto.InspectionResponse;

public interface CreateInspectionOnlyDataUseCase {
    InspectionResponse createInspectionOnlyData(InspectionFormRequest inspectionFormRequest);
}
