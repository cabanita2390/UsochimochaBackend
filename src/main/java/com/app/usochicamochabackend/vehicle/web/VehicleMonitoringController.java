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
@Tag(
                name = "Vehicle Monitoring",
                description = "Tablero consolidado (documentación SOAT/Tecno, aceite, última inspección) para la flota **no moto**: "
                                + "excluye filas cuyo tipo es MOTOCICLETA. Las motos usan `/api/v1/moto/monitoring/consolidated`.")
public class VehicleMonitoringController {

    private final VehicleMonitoringUseCase vehicleMonitoringUseCase;

    @GetMapping("/consolidated")
    @Operation(
                    summary = "Monitoreo consolidado (flota vehicular)",
                    description = "Una fila por vehículo no moto: vigencias de documentos, estado de aceite y datos derivados de última inspección. "
                                    + "Requiere rol MECANIC o ADMIN para GET.")
    public ResponseEntity<List<VehicleMonitoringDTO>> getConsolidated() {
        return ResponseEntity.ok(vehicleMonitoringUseCase.getConsolidatedMonitoring());
    }
}
