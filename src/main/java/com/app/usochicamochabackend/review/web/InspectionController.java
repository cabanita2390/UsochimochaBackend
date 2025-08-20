package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.review.application.service.InspectionService;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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

    /* --- CREATE --- */
    @PostMapping
    @Operation(summary = "Create inspection")
    @ApiResponse(responseCode = "201", description = "Inspection created")
    public ResponseEntity<InspectionEntity> createInspection(@RequestBody InspectionEntity inspectionEntity)
            throws URISyntaxException {
        InspectionEntity saved = inspectionService.createInspection(inspectionEntity);
        return ResponseEntity
                .created(new URI("/api/v1/inspection/" + saved.getId()))
                .body(saved);
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