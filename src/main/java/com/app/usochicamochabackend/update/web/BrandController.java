package com.app.usochicamochabackend.update.web;

import com.app.usochicamochabackend.update.application.dto.BrandRequest;
import com.app.usochicamochabackend.update.application.dto.BrandResponse;
import com.app.usochicamochabackend.update.application.port.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oil/brand")
@Tag(name = "Brands")
public class BrandController {

    private final CreateBrandUseCase createBrand;
    private final GetBrandByIdUseCase getBrandById;
    private final UpdateBrandUseCase updateBrand;
    private final DeleteBrandUseCase deleteBrand;
    private final GetAllBrandsUseCase getAllBrands;
    private final GetAllBrandsByTypeUseCase getAllBrandsByType;

    @Operation(summary = "Create a new brand", description = "Creates a new brand with type and name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BrandResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
    })
    @PostMapping
    public ResponseEntity<BrandResponse> create(@RequestBody BrandRequest brand) {
        BrandResponse response = createBrand.createBrand(brand);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Get a brand by ID", description = "Retrieve a brand using its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BrandResponse.class))),
            @ApiResponse(responseCode = "404", description = "Brand not found", content = @Content)
    })
    @GetMapping("/{id}")
    public BrandResponse getById(
            @Parameter(description = "ID of the brand to retrieve", example = "1")
            @PathVariable Long id) {
        return getBrandById.getBrandById(id);
    }

    @Operation(summary = "Update a brand", description = "Update the details of an existing brand by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BrandResponse.class))),
            @ApiResponse(responseCode = "404", description = "Brand not found", content = @Content)
    })
    @PutMapping("/{id}")
    public BrandResponse update(
            @Parameter(description = "ID of the brand to update", example = "1")
            @PathVariable Long id,
            @RequestBody BrandRequest brand) {
        return updateBrand.updateBrandById(id, brand);
    }

    @Operation(summary = "Delete a brand", description = "Performs a soft delete (sets status=false) of the brand by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Brand deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the brand to delete", example = "1")
            @PathVariable Long id) {
        deleteBrand.deleteBrandById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all brands", description = "Retrieve all active brands (status=true).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of brands retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BrandResponse.class)))
    })
    @GetMapping
    public List<BrandResponse> getAll() {
        return getAllBrands.getAllBrands();
    }

    @Operation(summary = "Get brands by type", description = "Retrieve all brands of a given type.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of brands retrieved by type",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BrandResponse.class)))
    })
    @GetMapping("/type/{type}")
    public List<BrandResponse> getAllByType(
            @Parameter(description = "Type of the brands to retrieve", example = "Electronics")
            @PathVariable String type) {
        return getAllBrandsByType.getAllBrandsByType(type);
    }
}