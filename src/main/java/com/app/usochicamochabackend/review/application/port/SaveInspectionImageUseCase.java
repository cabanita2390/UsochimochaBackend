package com.app.usochicamochabackend.review.application.port;

import com.app.usochicamochabackend.review.application.dto.ImageDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SaveInspectionImageUseCase {
    ImageDTO saveInspectionImage(Long inspectionId, MultipartFile image) throws IOException;
}
