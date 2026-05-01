package com.app.usochicamochabackend.vehicle.web;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleRequest;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;
import com.app.usochicamochabackend.vehicle.application.port.VehicleUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle")
@RequiredArgsConstructor
@Tag(name = "Vehicle", description = "Endpoints para vehículos")
public class VehicleController {

    private final VehicleUseCase vehicleUseCase;

    @GetMapping
    @Operation(summary = "Listar vehículos activos", description = "Retorna todos los vehículos activos con placa, marca y tipo.")
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleUseCase.findAllVehicles());
    }

    @GetMapping("/{placa}")
    @Operation(summary = "Obtener vehículo por placa", description = "Retorna el detalle de un vehículo específico.")
    public ResponseEntity<VehicleResponse> getVehicleByPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(vehicleUseCase.findByPlaca(placa));
    }

    @PostMapping
    @Operation(summary = "Crear vehículo", description = "Crea un nuevo vehículo en el sistema.")
    @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleUseCase.createVehicle(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar vehículo", description = "Actualiza la información de un vehículo existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo actualizado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable Integer id, @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleUseCase.updateVehicle(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar vehículo", description = "Elimina un vehículo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vehículo eliminado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<Void> deleteVehicle(@PathVariable Integer id) {
        vehicleUseCase.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
