package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.application.dto.ImageDTO;

import java.util.List;

public interface GetInspectionImagesUseCase {
    List<ImageDTO> getAllImagesByInspectionId(Long inspectionId);
}