package com.app.usochicamochabackend.vehicleinspection.web;

import com.app.usochicamochabackend.vehicleinspection.application.dto.VehicleDocumentRequest;
import com.app.usochicamochabackend.vehicleinspection.application.service.VehiculoInspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/documents")
@RequiredArgsConstructor
@Tag(
                name = "Admin Documents",
                description = "Actualización administrativa de vigencias de documentación (SOAT, Tecnomecánica, etc.) en vehículos/motos registrados. "
                                + "Requiere rol **ADMIN** (`POST /api/v1/admin/documents`).")
public class VehicleDocumentAdminController {

    private final VehiculoInspectionService vehiculoInspectionService;

    @PostMapping
    @Operation(summary = "Actualizar vigencia de documento", description = "Guarda o actualiza la fecha de vencimiento de SOAT/Tecno")
    public ResponseEntity<Void> updateDocument(@RequestBody VehicleDocumentRequest request) {
        vehiculoInspectionService.saveDocument(request);
        return ResponseEntity.ok().build();
    }
}
