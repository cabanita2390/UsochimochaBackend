package com.app.usochicamochabackend.execution.web;

import com.app.usochicamochabackend.execution.application.service.ResultService;
import com.app.usochicamochabackend.execution.infrastructure.entity.ResultEntity;
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
@RequestMapping("/api/v1/results")
@RequiredArgsConstructor
@Tag(name = "Results", description = "Endpoints for managing results")
public class ResultController {

    private final ResultService resultService;

    @GetMapping("/{id}")
    @Operation(summary = "Get result by ID")
    public ResponseEntity<ResultEntity> getResultById(@PathVariable Long id) {
        return ResponseEntity.ok(resultService.findResultById(id));
    }

    @GetMapping
    @Operation(summary = "Get all results")
    public ResponseEntity<List<ResultEntity>> getResults() {
        return ResponseEntity.ok(resultService.findAllResults());
    }

    @PostMapping
    @Operation(summary = "Create result")
    @ApiResponse(responseCode = "201", description = "Result created")
    public ResponseEntity<ResultEntity> createResult(@RequestBody ResultEntity resultEntity) throws URISyntaxException {
        ResultEntity saved = resultService.createResult(resultEntity);
        return ResponseEntity
                .created(new URI("/api/v1/results/" + saved.getId()))
                .body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update result")
    public ResponseEntity<ResultEntity> updateResult(
            @PathVariable Long id,
            @RequestBody ResultEntity resultEntity) throws URISyntaxException {

        resultEntity.setId(id);
        ResultEntity updated = resultService.updateResult(resultEntity);
        return ResponseEntity
                .created(new URI("/api/v1/results/" + updated.getId()))
                .body(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete result")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Result deleted")
    })
    public ResponseEntity<Void> deleteResult(@PathVariable Long id) {
        resultService.deleteResult(id);
        return ResponseEntity.noContent().build();
    }
}