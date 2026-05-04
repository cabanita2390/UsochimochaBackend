package com.app.usochicamochabackend.maintenance.web;

import com.app.usochicamochabackend.maintenance.application.dto.MaintenanceRequest;
import com.app.usochicamochabackend.maintenance.application.dto.MaintenanceResponse;
import com.app.usochicamochabackend.maintenance.application.port.MaintenanceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Gestión de mantenimientos preventivos y correctivos")
public class MaintenanceController {

    private final MaintenanceUseCase maintenanceUseCase;

    @PostMapping
    @Operation(summary = "Registrar un mantenimiento")
    public ResponseEntity<Void> registerMaintenance(@RequestBody MaintenanceRequest request) {
        maintenanceUseCase.registerMaintenance(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/motos")
    @Operation(summary = "Obtener historial de mantenimiento de motos")
    public ResponseEntity<List<MaintenanceResponse>> getMotosMaintenance() {
        return ResponseEntity.ok(maintenanceUseCase.getMotosMaintenance());
    }

    @GetMapping("/vehicles")
    @Operation(summary = "Obtener historial de mantenimiento de vehículos")
    public ResponseEntity<List<MaintenanceResponse>> getVehiclesMaintenance() {
        return ResponseEntity.ok(maintenanceUseCase.getVehiclesMaintenance());
    }
}
