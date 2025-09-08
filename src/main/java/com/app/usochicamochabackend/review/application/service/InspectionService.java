package com.app.usochicamochabackend.review.application.service;

import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.mapper.ImagesMapper;
import com.app.usochicamochabackend.mapper.InspectionMapper;
import com.app.usochicamochabackend.mapper.MachineMapper;
import com.app.usochicamochabackend.order.application.dto.GetAllOrdersByInspectionIdResponse;
import com.app.usochicamochabackend.review.application.dto.ImageDTO;
import com.app.usochicamochabackend.review.application.dto.InspectionDTO;
import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;
import com.app.usochicamochabackend.review.application.port.*;
import com.app.usochicamochabackend.review.infrastructure.entity.ImageEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.ImageRepository;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.review.web.InspectionStreamController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionService implements CreateInspectionOnlyDataUseCase, SaveInspectionImageUseCase, GetInspectionByIdUseCase, GetInspectionImagesUseCase, GetAllInspectionsWithoutImagesUseCase {

    private final InspectionStreamController inspectionStreamController;
    private final InspectionRepository inspectionRepository;
    private final UserRepositoryJpa userRepository;
    private final MachineRepository machineRepository;
    private final ImageRepository imageRepository;

    @Override
    public InspectionFormResponse createInspectionOnlyData(InspectionFormRequest request) {
        InspectionEntity entity = InspectionMapper.toEntityWithoutOrdersAndImages(request, userRepository, machineRepository);
        InspectionEntity saved = inspectionRepository.save(entity);
        InspectionFormResponse inspectionResponse = InspectionMapper.toDto(saved);

        //Pending to implement notifications for soat and runt

        if (Boolean.TRUE.equals(saved.getUnexpected())) {
            inspectionStreamController.publish(inspectionResponse);
        }

        return inspectionResponse;
    }

    @Override
    public ImageDTO saveInspectionImage(Long inspectionId, MultipartFile image) throws IOException {
        InspectionEntity inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found"));

        String uuid = inspection.getUUID();

        Path uploadsPath = Paths.get("uploads");
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }

        Path inspectionFolder = uploadsPath.resolve(uuid);
        if (!Files.exists(inspectionFolder)) {
            Files.createDirectories(inspectionFolder);
        }

        if (inspection.getImages() != null && !inspection.getImages().isEmpty()) {
            ImageEntity lastImage = inspection.getImages().get(inspection.getImages().size() - 1);
            Path lastImagePath = Paths.get(lastImage.getUrl());

            if (Files.exists(lastImagePath)) {
                if (isDuplicateHalfImage(image, lastImagePath)) {
                    throw new IllegalArgumentException("Duplicate image detected");
                }
            }
        }

        int nextIndex = inspection.getImages() != null ? inspection.getImages().size() + 1 : 1;
        String extension = getFileExtension(image);

        String filename = uuid + "-" + nextIndex + extension;
        Path filePath = inspectionFolder.resolve(filename);

        Files.write(filePath, image.getBytes());

        ImageEntity img = new ImageEntity();
        img.setUrl(filePath.toString());
        img.setInspection(inspection);

        if (inspection.getImages() == null) {
            inspection.setImages(new ArrayList<>());
        }
        inspection.getImages().add(img);

        InspectionEntity inspectionEntity = inspectionRepository.save(inspection);
        List<ImageEntity> images = inspectionEntity.getImages();
        ImageEntity lastImage = images.get(images.size() - 1);
        img.setId(lastImage.getId());

        return ImagesMapper.toDto(img);
    }

    private boolean isDuplicateHalfImage(MultipartFile newImage, Path lastImagePath) throws IOException {
        BufferedImage newImg = ImageIO.read(newImage.getInputStream());
        BufferedImage lastImg = ImageIO.read(lastImagePath.toFile());

        if (newImg == null || lastImg == null) return false;

        int width = Math.min(newImg.getWidth(), lastImg.getWidth());
        int height = Math.min(newImg.getHeight(), lastImg.getHeight());

        BufferedImage newHalf = newImg.getSubimage(0, 0, width, height / 2);
        BufferedImage lastHalf = lastImg.getSubimage(0, 0, width, height / 2);

        for (int y = 0; y < newHalf.getHeight(); y++) {
            for (int x = 0; x < newHalf.getWidth(); x++) {
                if (newHalf.getRGB(x, y) != lastHalf.getRGB(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public InspectionDTO getInspectionById(Long inspectionId) {
        InspectionEntity inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found"));

        return InspectionMapper.toDtoWithImagesAndOrders(inspection);
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

    @Override
    public List<InspectionFormResponse> getAllInspectionsWithoutImages() {
        List<InspectionEntity> inspectionEntityList = inspectionRepository.findAll();
        return InspectionMapper.toDtoListWithoutImagesAndOrders(inspectionEntityList);
    }

    @Override
    public List<ImageDTO> getAllImagesByInspectionId(Long inspectionId) {
        List<ImageEntity> imageEntitiesList = imageRepository.findByInspectionId(inspectionId);
        return ImagesMapper.toDtoList(imageEntitiesList);
    }
}