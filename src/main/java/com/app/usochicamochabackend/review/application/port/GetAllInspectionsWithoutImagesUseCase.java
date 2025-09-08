package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;

import java.util.List;

public interface GetAllInspectionsWithoutImagesUseCase {
    List<InspectionFormResponse> getAllInspectionsWithoutImages();
}
