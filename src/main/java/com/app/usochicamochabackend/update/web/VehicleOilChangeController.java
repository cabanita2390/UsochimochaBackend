package com.app.usochicamochabackend.update.web;

import com.app.usochicamochabackend.update.application.dto.VehicleOilChangeRequest;
import com.app.usochicamochabackend.update.application.service.VehicleOilChangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicle/oil-change")
@RequiredArgsConstructor
@Tag(name = "Vehicle Oil Change", description = "Endpoints para registrar cambios de aceite en vehículos")
public class VehicleOilChangeController {

    private final VehicleOilChangeService vehicleOilChangeService;

    @PostMapping
    @Operation(summary = "Registrar cambio de aceite", description = "Registra un nuevo cambio de aceite para un vehículo")
    public ResponseEntity<Void> registerChange(@RequestBody VehicleOilChangeRequest request) {
        vehicleOilChangeService.registerChange(request);
        return ResponseEntity.ok().build();
    }
}
