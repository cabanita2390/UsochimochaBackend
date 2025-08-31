package com.app.usochicamochabackend.review.application.service;

import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.mapper.ImagesMapper;
import com.app.usochicamochabackend.mapper.InspectionMapper;
import com.app.usochicamochabackend.mapper.OrderMapper;
import com.app.usochicamochabackend.review.application.dto.ImageDTO;
import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.dto.InspectionResponse;
import com.app.usochicamochabackend.review.application.port.*;
import com.app.usochicamochabackend.review.infrastructure.entity.ImageEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.review.web.InspectionStreamController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InspectionService implements CreateInspectionOnlyDataUseCase, SaveInspectionImageUseCase, GetInspectionByIdUseCase, GetInspectionImagesUseCase, GetAllInspectionsWithoutImagesUseCase {

    private final InspectionRepository inspectionRepository;
    private final UserRepositoryJpa userRepository;
    private final MachineRepository machineRepository;
    private final InspectionStreamController inspectionStreamController;

    @Override
    public InspectionResponse createInspectionOnlyData(InspectionFormRequest request) {
        InspectionEntity entity = InspectionMapper.toEntity(request, userRepository, machineRepository);
        InspectionEntity saved = inspectionRepository.save(entity);
        InspectionResponse inspectionResponse = InspectionMapper.toDtoWithoutOrder(saved);

        if (Boolean.TRUE.equals(saved.getUnexpected())) {
            inspectionStreamController.publish(inspectionResponse);
        }

        return inspectionResponse;
    }

    @Override
    public ImageDTO saveInspectionImage(Long inspectionId, String uuid, MultipartFile image) throws IOException {
        InspectionEntity inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found"));

        if (!inspection.getUUID().equals(uuid)) {
            throw new IllegalArgumentException("UUID does not match with inspection");
        }

        Path uploadsPath = Paths.get("uploads");
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }

        Path inspectionFolder = uploadsPath.resolve(uuid);
        if (!Files.exists(inspectionFolder)) {
            Files.createDirectories(inspectionFolder);
        }

        int nextIndex = inspection.getImages() != null ? inspection.getImages().size() + 1 : 1;
        String extension = getFileExtension(image);

        String filename = uuid + "-" + nextIndex + extension;
        Path filePath = inspectionFolder.resolve(filename);

        Files.write(filePath, image.getBytes());

        ImageEntity img = new ImageEntity();
        img.setUrl(filePath.toString());
        img.setUuid(uuid);
        img.setInspection(inspection);

        if (inspection.getImages() == null) {
            inspection.setImages(new ArrayList<>());
        }
        inspection.getImages().add(img);

        inspectionRepository.save(inspection);

        return ImagesMapper.toDto(img);
    }

    @Override
    public InspectionResponse getInspectionById(Long inspectionId) {
        InspectionEntity inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found"));

        return InspectionMapper.toDto(inspection);
    }

    @Override
    public List<ImageDTO> getInspectionImages(Long inspectionId) {
        InspectionEntity inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found"));
        return inspection.getImages().stream()
                .map(ImagesMapper::toDto)
                .toList();
    }

    @Override
    public List<InspectionResponse> getAllInspectionsWithoutImages() {
        return inspectionRepository.findAll().stream()
                .map(InspectionMapper::toDtoWithoutImages)
                .toList();
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String contentType = file.getContentType();
        if ("image/png".equals(contentType)) {
            return ".png";
        } else if ("image/jpeg".equals(contentType)) {
            return ".jpg";
        }
        return ".bin"; // fallback
    }
}