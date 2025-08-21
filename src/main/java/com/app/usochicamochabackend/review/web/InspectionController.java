package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.service.InspectionService;
import com.app.usochicamochabackend.review.infrastructure.entity.ImageEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inspection")
@RequiredArgsConstructor
@Tag(name = "Inspection", description = "Endpoints for managing inspections")
public class InspectionController {

    private final InspectionService inspectionService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Crear una nueva inspección",
            description = "Este endpoint permite crear una inspección únicamente con datos JSON (sin imágenes)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inspección creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<InspectionEntity> createInspection(
            @RequestBody InspectionFormRequest request
    ) throws URISyntaxException {

        InspectionEntity saved = inspectionService.createInspectionOnlyData(request);

        return ResponseEntity
                .created(new URI("/api/v1/inspection/" + saved.getId()))
                .body(saved);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir una imagen de una inspección",
            description = "Este endpoint permite subir una sola imagen para una inspección existente. " +
                    "Requiere el ID y el UUID de la inspección."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Imagen subida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "404", description = "Inspección no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ImageEntity> uploadInspectionImage(
            @Parameter(description = "ID de la inspección", required = true)
            @PathVariable("id") Long inspectionId,

            @Parameter(description = "UUID de la inspección", required = true)
            @RequestParam("uuid") String uuid,

            @Parameter(description = "Imagen a subir", required = true)
            @RequestPart("imagen") MultipartFile imagen
    ) throws IOException, URISyntaxException {

        ImageEntity savedImage = inspectionService.saveInspectionImage(inspectionId, uuid, imagen);

        return ResponseEntity
                .created(new URI("/api/v1/inspection/" + inspectionId + "/image/" + savedImage.getId()))
                .body(savedImage);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una inspección por ID",
            description = "Devuelve toda la información de una inspección, incluyendo usuario y máquina asociada."
    )
    public ResponseEntity<InspectionEntity> getInspectionById(
            @PathVariable Long id
    ) {
        InspectionEntity inspection = inspectionService.getInspectionById(id);
        return ResponseEntity.ok(inspection);
    }

    @GetMapping("/{id}/imagenes")
    @Operation(
            summary = "Obtener imágenes de una inspección",
            description = "Devuelve todas las imágenes asociadas a una inspección."
    )
    public ResponseEntity<List<ImageEntity>> getInspectionImages(
            @PathVariable Long id
    ) {
        List<ImageEntity> images = inspectionService.getInspectionImages(id);
        return ResponseEntity.ok(images);
    }

    @GetMapping
    @Operation(
            summary = "Obtener todas las inspecciones",
            description = "Devuelve todas las inspecciones sin incluir las imágenes"
    )
    public ResponseEntity<List<InspectionEntity>> getAllInspections() {
        List<InspectionEntity> inspections = inspectionService.getAllInspectionsWithoutImages();
        return ResponseEntity.ok(inspections);
    }
}
