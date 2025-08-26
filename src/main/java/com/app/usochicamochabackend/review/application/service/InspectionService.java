package com.app.usochicamochabackend.review.application.service;

import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.review.application.dto.ImageDTO;
import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.infrastructure.entity.ImageEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
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
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final UserRepositoryJpa userRepository;
    private final MachineRepository machineRepository;

    public InspectionEntity createInspectionOnlyData(InspectionFormRequest request) {
        InspectionEntity entity = mapToEntity(request, null);
        return inspectionRepository.save(entity);
    }

    public ImageEntity saveInspectionImage(Long inspectionId, String uuid, MultipartFile imagen) throws IOException {
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

        String extension = getFileExtension(imagen);

        String filename = uuid + "-" + nextIndex + extension;
        Path filePath = inspectionFolder.resolve(filename);

        Files.write(filePath, imagen.getBytes());

        ImageEntity img = new ImageEntity();
        img.setUrl(filePath.toString());
        img.setUuid(uuid);
        img.setInspection(inspection);

        if (inspection.getImages() == null) {
            inspection.setImages(new ArrayList<>());
        }
        inspection.getImages().add(img);

        inspectionRepository.save(inspection);

        return img;
    }

    public InspectionEntity getInspectionById(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found"));
    }


    public List<ImageDTO> getInspectionImages(Long id) {
        InspectionEntity inspection = inspectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found"));
        return inspection.getImages().stream().map(image -> new ImageDTO(image.getUrl(), image.getUuid(), image.getInspection().getId())).toList();
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

    private InspectionEntity mapToEntity(InspectionFormRequest request, List<String> imagePaths) {
        InspectionEntity entity = new InspectionEntity();

        entity.setUUID(request.UUID());
        entity.setDateStamp(request.dateStamp());
        entity.setHourmeter(request.hourmeter());
        entity.setBrakeStatus(request.brakeStatus());
        entity.setLeakStatus(request.leakStatus());
        entity.setBeltsPulleysStatus(request.beltsPulleysStatus());
        entity.setTireLanesStatus(request.tireLanesStatus());
        entity.setCarIgnitionStatus(request.carIgnitionStatus());
        entity.setElectricalStatus(request.electricalStatus());
        entity.setMechanicalStatus(request.mechanicalStatus());
        entity.setTemperatureStatus(request.temperatureStatus());
        entity.setOilStatus(request.oilStatus());
        entity.setHydraulicStatus(request.hydraulicStatus());
        entity.setCoolantStatus(request.coolantStatus());
        entity.setStructuralStatus(request.structuralStatus());
        entity.setExpirationDateFireExtinguisher(request.expirationDateFireExtinguisher());
        entity.setObservations(request.observations());

        if (request.userId() != null) {
            entity.setUser(userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found")));
        }

        if (request.machineId() != null) {
            entity.setMachine(machineRepository.findById(request.machineId())
                    .orElseThrow(() -> new IllegalArgumentException("Machine not found")));
        }

        return entity;
    }

    public List<InspectionEntity> getAllInspectionsWithoutImages() {
        List<InspectionEntity> inspections = inspectionRepository.findAll();

        // Crear una copia de cada inspección sin la lista de imágenes
        List<InspectionEntity> result = new ArrayList<>();
        for (InspectionEntity insp : inspections) {
            InspectionEntity copy = new InspectionEntity();

            copy.setId(insp.getId());
            copy.setUUID(insp.getUUID());
            copy.setDateStamp(insp.getDateStamp());
            copy.setHourmeter(insp.getHourmeter());
            copy.setLeakStatus(insp.getLeakStatus());
            copy.setBrakeStatus(insp.getBrakeStatus());
            copy.setBeltsPulleysStatus(insp.getBeltsPulleysStatus());
            copy.setTireLanesStatus(insp.getTireLanesStatus());
            copy.setCarIgnitionStatus(insp.getCarIgnitionStatus());
            copy.setElectricalStatus(insp.getElectricalStatus());
            copy.setMechanicalStatus(insp.getMechanicalStatus());
            copy.setTemperatureStatus(insp.getTemperatureStatus());
            copy.setOilStatus(insp.getOilStatus());
            copy.setHydraulicStatus(insp.getHydraulicStatus());
            copy.setCoolantStatus(insp.getCoolantStatus());
            copy.setStructuralStatus(insp.getStructuralStatus());
            copy.setExpirationDateFireExtinguisher(insp.getExpirationDateFireExtinguisher());
            copy.setObservations(insp.getObservations());
            copy.setUser(insp.getUser());
            copy.setMachine(insp.getMachine());
            copy.setImages(null);

            result.add(copy);
        }

        return result;
    }

}
