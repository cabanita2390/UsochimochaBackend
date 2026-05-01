package com.app.usochicamochabackend.vehicle.web;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleMonitoringDTO;
import com.app.usochicamochabackend.vehicle.application.port.VehicleMonitoringUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle/monitoring")
@RequiredArgsConstructor
@Tag(name = "Vehicle Monitoring", description = "Endpoints para el seguimiento y control consolidado de vehículos")
public class VehicleMonitoringController {

    private final VehicleMonitoringUseCase vehicleMonitoringUseCase;

    @GetMapping("/consolidated")
    @Operation(summary = "Obtener monitoreo consolidado", description = "Retorna el estado de documentos y mantenimiento de todos los vehículos")
    public ResponseEntity<List<VehicleMonitoringDTO>> getConsolidated() {
        return ResponseEntity.ok(vehicleMonitoringUseCase.getConsolidatedMonitoring());
    }
}
