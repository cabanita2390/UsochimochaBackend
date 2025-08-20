package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.service.InspectionService;
import com.app.usochicamochabackend.review.infrastructure.entity.ImageEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inspection")
@RequiredArgsConstructor
@Tag(name = "Inspection", description = "Endpoints for managing inspections")
public class InspectionController {

    private final InspectionService inspectionService;

    /* --- READ --- */
    @GetMapping("/{id}")
    @Operation(summary = "Get inspection by ID")
    public ResponseEntity<InspectionEntity> getInspectionById(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionService.findInspectionById(id));
    }

    @GetMapping
    @Operation(summary = "Get all inspections")
    public ResponseEntity<List<InspectionEntity>> getInspections() {
        return ResponseEntity.ok(inspectionService.findAllInspections());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new inspection",
            description = "Send `data` (JSON) + optional `imagenes` (files). " +
                    "`data` se deserializa a InspectionFormRequest; " +
                    "las imágenes se guardan en `uploads/{uuid}/img_N.ext`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inspection created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InspectionEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<InspectionEntity> createInspection(
            @Parameter(
                    name = "data",
                    description = "Inspection data as JSON",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InspectionFormRequest.class),
                            examples = @ExampleObject(value = """
            {
              "horometro": "1200",
              "estadoFugas": "OK",
              "estadoFrenos": "Revisar",
              "estadoCorreasPoleas": "OK",
              "estadoLlantasCarriles": "OK",
              "estadoEncendido": "OK",
              "estadoElectrico": "OK",
              "estadoMecanico": "OK",
              "estadoTemperatura": "OK",
              "estadoAceite": "OK",
              "estadoHidraulico": "OK",
              "estadoRefrigerante": "OK",
              "estadoEstructural": "OK",
              "vigenciaExtintor": "2025-12-31",
              "observaciones": "Todo bien menos los frenos"
            }
            """)
                    )
            )
            @RequestPart("data") InspectionFormRequest request,

            @Parameter(
                    name = "imagenes",
                    description = "Imagenes de la inspección (jpg/png/etc.)",
                    required = false,
                    content = @Content(
                            // cada parte de archivo es binaria
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                    )
            )
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes
    ) throws URISyntaxException, IOException {

        // 1) Crear carpeta uploads/{uuid}
        String uuid = UUID.randomUUID().toString();
        Path basePath = Paths.get("uploads").resolve(uuid);
        Files.createDirectories(basePath);

        // 2) Guardar imágenes como img_1.ext, img_2.ext, ...
        List<ImageEntity> imageEntities = new ArrayList<>();
        if (imagenes != null && !imagenes.isEmpty()) {
            int index = 1;
            for (MultipartFile file : imagenes) {
                if (file == null || file.isEmpty()) continue;

                String ext = getExtension(file.getOriginalFilename());
                if (ext.isBlank()) ext = ".jpg"; // fallback
                String filename = "img_" + index + ext;

                Path filePath = basePath.resolve(filename);
                try (InputStream in = file.getInputStream()) {
                    Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                }

                ImageEntity img = new ImageEntity();
                // guarda ruta relativa; si expones /uploads/** con ResourceHandler será clickeable
                img.setUrl("uploads/" + uuid + "/" + filename);
                imageEntities.add(img);
                index++;
            }
        }

        // 3) Mapear DTO -> Entity
        InspectionEntity inspectionEntity = new InspectionEntity();
        inspectionEntity.setUuid(uuid);
        inspectionEntity.setDateStamp(LocalDateTime.now());
        inspectionEntity.setHorometro(request.horometro());
        inspectionEntity.setEstadoFugas(request.estadoFugas());
        inspectionEntity.setEstadoFrenos(request.estadoFrenos());
        inspectionEntity.setEstadoCorreasPoleas(request.estadoCorreasPoleas());
        inspectionEntity.setEstadoLlantasCarriles(request.estadoLlantasCarriles());
        inspectionEntity.setEstadoEncendido(request.estadoEncendido());
        inspectionEntity.setEstadoElectrico(request.estadoElectrico());
        inspectionEntity.setEstadoMecanico(request.estadoMecanico());
        inspectionEntity.setEstadoTemperatura(request.estadoTemperatura());
        inspectionEntity.setEstadoAceite(request.estadoAceite());
        inspectionEntity.setEstadoHidraulico(request.estadoHidraulico());
        inspectionEntity.setEstadoRefrigerante(request.estadoRefrigerante());
        inspectionEntity.setEstadoEstructural(request.estadoEstructural());
        inspectionEntity.setVigenciaExtintor(request.vigenciaExtintor());
        inspectionEntity.setObservaciones(request.observaciones());
        inspectionEntity.setImages(imageEntities);

        // 4) Persistir y responder
        InspectionEntity saved = inspectionService.createInspection(inspectionEntity);
        return ResponseEntity
                .created(new URI("/api/v1/inspection/" + saved.getId()))
                .body(saved);
    }

    /** Devuelve la extensión con punto (".jpg"), o "" si no hay. */
    private static String getExtension(String originalFilename) {
        if (originalFilename == null) return "";
        int dot = originalFilename.lastIndexOf('.');
        return (dot >= 0 ? originalFilename.substring(dot) : "");
    }

    /* --- UPDATE --- */
    @PutMapping("/{id}")
    @Operation(summary = "Update inspection")
    public ResponseEntity<InspectionEntity> updateInspection(
            @PathVariable Long id,
            @RequestBody InspectionEntity inspectionEntity) throws URISyntaxException {

        inspectionEntity.setId(id);
        InspectionEntity updated = inspectionService.updateInspection(inspectionEntity);
        return ResponseEntity
                .created(new URI("/api/v1/inspection/" + updated.getId()))
                .body(updated);
    }

    /* --- DELETE --- */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete inspection")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inspection deleted")
    })
    public ResponseEntity<Void> deleteInspection(@PathVariable Long id) {
        inspectionService.deleteInspection(id);
        return ResponseEntity.noContent().build();
    }
}