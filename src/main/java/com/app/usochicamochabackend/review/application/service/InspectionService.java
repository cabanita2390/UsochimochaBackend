package com.app.usochicamochabackend.review.application.service;

import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
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

        if (!inspection.getUuid().equals(uuid)) {
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


    public List<ImageEntity> getInspectionImages(Long id) {
        InspectionEntity inspection = inspectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found"));
        return inspection.getImages();
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

        entity.setUuid(UUID.randomUUID().toString());
        entity.setDateStamp(LocalDateTime.now());
        entity.setHorometro(request.horometro());
        entity.setEstadoFrenos(request.estadoFrenos());
        entity.setEstadoFugas(request.estadoFugas());
        entity.setEstadoCorreasPoleas(request.estadoCorreasPoleas());
        entity.setEstadoLlantasCarriles(request.estadoLlantasCarriles());
        entity.setEstadoEncendido(request.estadoEncendido());
        entity.setEstadoElectrico(request.estadoElectrico());
        entity.setEstadoMecanico(request.estadoMecanico());
        entity.setEstadoTemperatura(request.estadoTemperatura());
        entity.setEstadoAceite(request.estadoAceite());
        entity.setEstadoHidraulico(request.estadoHidraulico());
        entity.setEstadoRefrigerante(request.estadoRefrigerante());
        entity.setEstadoEstructural(request.estadoEstructural());
        entity.setVigenciaExtintor(request.vigenciaExtintor());
        entity.setObservaciones(request.observaciones());

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
            copy.setUuid(insp.getUuid());
            copy.setDateStamp(insp.getDateStamp());
            copy.setHorometro(insp.getHorometro());
            copy.setEstadoFugas(insp.getEstadoFugas());
            copy.setEstadoFrenos(insp.getEstadoFrenos());
            copy.setEstadoCorreasPoleas(insp.getEstadoCorreasPoleas());
            copy.setEstadoLlantasCarriles(insp.getEstadoLlantasCarriles());
            copy.setEstadoEncendido(insp.getEstadoEncendido());
            copy.setEstadoElectrico(insp.getEstadoElectrico());
            copy.setEstadoMecanico(insp.getEstadoMecanico());
            copy.setEstadoTemperatura(insp.getEstadoTemperatura());
            copy.setEstadoAceite(insp.getEstadoAceite());
            copy.setEstadoHidraulico(insp.getEstadoHidraulico());
            copy.setEstadoRefrigerante(insp.getEstadoRefrigerante());
            copy.setEstadoEstructural(insp.getEstadoEstructural());
            copy.setVigenciaExtintor(insp.getVigenciaExtintor());
            copy.setObservaciones(insp.getObservaciones());
            copy.setUser(insp.getUser());
            copy.setMachine(insp.getMachine());
            copy.setImages(null);

            result.add(copy);
        }

        return result;
    }

}
