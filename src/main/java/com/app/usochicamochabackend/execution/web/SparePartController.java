package com.app.usochicamochabackend.execution.web;

import com.app.usochicamochabackend.execution.application.service.SparePartService;
import com.app.usochicamochabackend.execution.infrastructure.entity.SparePartEntity;
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
@RequestMapping("/api/v1/spare-parts")
@RequiredArgsConstructor
@Tag(name = "SpareParts", description = "Endpoints for managing spare parts")
public class SparePartController {

    private final SparePartService sparePartService;

    @GetMapping("/{id}")
    @Operation(summary = "Get spare part by ID")
    public ResponseEntity<SparePartEntity> getSparePartById(@PathVariable Long id) {
        return ResponseEntity.ok(sparePartService.findSparePartById(id));
    }

    @GetMapping
    @Operation(summary = "Get all spare parts")
    public ResponseEntity<List<SparePartEntity>> getSpareParts() {
        return ResponseEntity.ok(sparePartService.findAllSpareParts());
    }

    @PostMapping
    @Operation(summary = "Create spare part")
    @ApiResponse(responseCode = "201", description = "Spare part created")
    public ResponseEntity<SparePartEntity> createSparePart(@RequestBody SparePartEntity sparePartEntity) throws URISyntaxException {
        SparePartEntity saved = sparePartService.createSparePart(sparePartEntity);
        return ResponseEntity
                .created(new URI("/api/v1/spare-parts/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update spare part")
    public ResponseEntity<SparePartEntity> updateSparePart(
            @PathVariable Long id,
            @RequestBody SparePartEntity sparePartEntity) throws URISyntaxException {

        sparePartEntity.setId(id);
        SparePartEntity updated = sparePartService.updateSparePart(sparePartEntity);
        return ResponseEntity
                .created(new URI("/api/v1/spare-parts/" + updated.getId()))
                .body(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete spare part")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Spare part deleted")
    })
    public ResponseEntity<Void> deleteSparePart(@PathVariable Long id) {
        sparePartService.deleteSparePart(id);
        return ResponseEntity.noContent().build();
    }
}