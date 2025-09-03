package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.mapper.ImagesMapper;
import com.app.usochicamochabackend.mapper.InspectionMapper;
import com.app.usochicamochabackend.review.application.dto.ImageDTO;
import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.dto.InspectionResponse;
import com.app.usochicamochabackend.review.application.port.*;
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

    private final CreateInspectionOnlyDataUseCase createInspectionOnlyDataUseCase;
    private final GetAllInspectionsWithoutImagesUseCase  getAllInspectionsWithoutImagesUseCase;
    private final GetInspectionByIdUseCase getInspectionByIdUseCase;
    private final GetInspectionImagesUseCase  getInspectionImagesUseCase;
    private final SaveInspectionImageUseCase  saveInspectionImageUseCase;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create a new inspection",
            description = "This endpoint allows creating an inspection using only JSON data (without images)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inspection successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<InspectionResponse> createInspection(@RequestBody InspectionFormRequest request) throws URISyntaxException {
        InspectionResponse saved = createInspectionOnlyDataUseCase.createInspectionOnlyData(request);

        return ResponseEntity
                .created(new URI("/api/v1/inspection/" + saved.id()))
                .body(saved);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload an inspection image",
            description = "This endpoint allows uploading a single image for an existing inspection. " +
                    "Requires the inspection ID and UUID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image successfully uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Inspection not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImageDTO> uploadInspectionImage(
            @Parameter(description = "Inspection ID", required = true)
            @PathVariable("id") Long inspectionId,
            @Parameter(description = "Inspection UUID", required = true)
            @RequestParam("uuid") String uuid,
            @Parameter(description = "Image to upload", required = true)
            @RequestPart("imagen") MultipartFile imagen
    ) throws IOException, URISyntaxException {

        ImageDTO savedImage = saveInspectionImageUseCase.saveInspectionImage(inspectionId, imagen);

        return ResponseEntity
                .created(new URI("/api/v1/inspection/" + inspectionId + "/image/" + savedImage.id()))
                .body(savedImage);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get an inspection by ID",
            description = "Returns all the information of an inspection, including the associated user and machine."
    )
    public ResponseEntity<InspectionResponse> getInspectionById(@PathVariable Long id) {
        InspectionResponse inspection = getInspectionByIdUseCase.getInspectionById(id);
        return ResponseEntity.ok(inspection);
    }

    @GetMapping("/{id}/images")
    @Operation(
            summary = "Get inspection images",
            description = "Returns all images associated with an inspection."
    )
    public ResponseEntity<List<ImageDTO>> getInspectionImages(@PathVariable Long id) {
        return ResponseEntity.ok(getInspectionImagesUseCase.getInspectionImages(id));
    }

    @GetMapping
    @Operation(
            summary = "Get all inspections",
            description = "Returns all inspections without including images."
    )
    public ResponseEntity<List<InspectionResponse>> getAllInspections() {
        List<InspectionResponse> inspections = getAllInspectionsWithoutImagesUseCase.getAllInspectionsWithoutImages();
        return ResponseEntity.ok(inspections);
    }

}
