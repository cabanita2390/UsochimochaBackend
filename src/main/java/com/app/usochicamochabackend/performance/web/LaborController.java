package com.app.usochicamochabackend.performance.web;

import com.app.usochicamochabackend.performance.application.service.LaborService;
import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
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
@RequestMapping("/api/v1/labors")
@RequiredArgsConstructor
@Tag(name = "Labors", description = "Endpoints for managing labors")
public class LaborController {

    private final LaborService laborService;

    @GetMapping("/{id}")
    @Operation(summary = "Get labor by ID")
    public ResponseEntity<LaborEntity> getLaborById(@PathVariable Long id) {
        return ResponseEntity.ok(laborService.findLaborById(id));
    }

    @GetMapping
    @Operation(summary = "Get all labors")
    public ResponseEntity<List<LaborEntity>> getLabors() {
        return ResponseEntity.ok(laborService.findAllLabors());
    }

    @PostMapping
    @Operation(summary = "Create labor")
    @ApiResponse(responseCode = "201", description = "Labor created")
    public ResponseEntity<LaborEntity> createLabor(@RequestBody LaborEntity laborEntity) throws URISyntaxException {
        LaborEntity saved = laborService.createLabor(laborEntity);
        return ResponseEntity
                .created(new URI("/api/v1/labors/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update labor")
    public ResponseEntity<LaborEntity> updateLabor(
            @PathVariable Long id,
            @RequestBody LaborEntity laborEntity) throws URISyntaxException {

        laborEntity.setId(id);
        LaborEntity updated = laborService.updateLabor(laborEntity);
        return ResponseEntity
                .created(new URI("/api/v1/labors/" + updated.getId()))
                .body(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete labor")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Labor deleted")
    })
    public ResponseEntity<Void> deleteLabor(@PathVariable Long id) {
        laborService.deleteLabor(id);
        return ResponseEntity.noContent().build();
    }
}