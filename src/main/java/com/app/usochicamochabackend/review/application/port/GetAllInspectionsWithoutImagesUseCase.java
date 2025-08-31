package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.application.dto.InspectionResponse;

import java.util.List;

public interface GetAllInspectionsWithoutImagesUseCase {
    List<InspectionResponse> getAllInspectionsWithoutImages();
}
