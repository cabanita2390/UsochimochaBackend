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

    public InspectionEntity createInspection(InspectionFormRequest request, List<MultipartFile> imagenes) throws IOException {
        // Crear carpeta de uploads si no existe
        Path uploadsPath = Paths.get("uploads");
        if (!Files.exists(uploadsPath)) {
            Files.createDirectories(uploadsPath);
        }

        // Crear carpeta única para la inspección
        String inspectionId = UUID.randomUUID().toString();
        Path inspectionFolder = uploadsPath.resolve(inspectionId);
        Files.createDirectories(inspectionFolder);

        List<String> imagePaths = saveImages(imagenes, inspectionFolder);

        // Guardar entidad en base de datos
        InspectionEntity entity = mapToEntity(request, imagePaths);
        return inspectionRepository.save(entity);
    }

    private List<String> saveImages(List<MultipartFile> imagenes, Path inspectionFolder) throws IOException {
        List<String> imagePaths = new ArrayList<>();

        if (imagenes != null && !imagenes.isEmpty()) {
            int index = 1;
            for (MultipartFile imagen : imagenes) {
                if (!imagen.isEmpty()) {
                    // Detectar extensión
                    String extension = getFileExtension(imagen);

                    // Guardar con nombre seguro
                    Path filePath = inspectionFolder.resolve("img_" + index + extension);
                    Files.write(filePath, imagen.getBytes());
                    imagePaths.add(filePath.toString());
                    index++;
                }
            }
        }

        return imagePaths;
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

        // Mapeo DTO → Entity
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

        // Mapeo de User
        if (request.userId() != null) {
            entity.setUser(userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found")));
        }

        // Mapeo de Machine
        if (request.machineId() != null) {
            entity.setMachine(machineRepository.findById(request.machineId())
                    .orElseThrow(() -> new IllegalArgumentException("Machine not found")));
        }

        // Mapeo de imágenes
        if (imagePaths != null && !imagePaths.isEmpty()) {
            List<ImageEntity> images = imagePaths.stream()
                    .map(path -> {
                        ImageEntity img = new ImageEntity();
                        img.setUrl(path);
                        img.setInspection(entity); // relación inversa si la tienes en ImageEntity
                        return img;
                    })
                    .toList();
            entity.setImages(images);
        }

        return entity;
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

}
