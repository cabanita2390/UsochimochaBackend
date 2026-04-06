package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GetAllInspectionsWithoutImagesUseCase {
    Page<InspectionFormResponse> getAllInspectionsWithoutImages(Pageable pageable);
}
