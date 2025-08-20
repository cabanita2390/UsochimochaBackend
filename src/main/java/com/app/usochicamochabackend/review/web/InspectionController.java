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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Crear una nueva inspección",
            description = "Este endpoint permite crear una inspección con datos en formato JSON y múltiples imágenes en formato multipart."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inspección creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<InspectionEntity> createInspection(
            @Parameter(description = "Datos de la inspección en formato JSON", required = true)
            @RequestPart("data") InspectionFormRequest request,

            @Parameter(description = "Lista de imágenes opcionales en formato multipart", required = false)
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes
    ) throws URISyntaxException, IOException {

        InspectionEntity saved = inspectionService.createInspection(request, imagenes);

        return ResponseEntity
                .created(new URI("/api/v1/inspection/" + saved.getId()))
                .body(saved);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una inspección por ID",
            description = "Devuelve toda la información de una inspección, incluyendo usuario y máquina asociada"
    )
    public ResponseEntity<InspectionEntity> getInspectionById(@PathVariable Long id) {
        InspectionEntity inspection = inspectionService.getInspectionById(id);
        return ResponseEntity.ok(inspection);
    }

    @GetMapping("/{id}/imagenes")
    @Operation(
            summary = "Obtener imágenes de una inspección",
            description = "Devuelve todas las imágenes asociadas a una inspección"
    )
    public ResponseEntity<List<ImageEntity>> getInspectionImages(@PathVariable Long id) {
        List<ImageEntity> images = inspectionService.getInspectionImages(id);
        return ResponseEntity.ok(images);
    }

}