package com.app.usochicamochabackend.vehicle.web;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;
import com.app.usochicamochabackend.vehicle.application.port.FindAllVehiclesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicle")
@RequiredArgsConstructor
@Tag(name = "Vehicle", description = "Endpoints para vehículos")
public class VehicleController {

    private final FindAllVehiclesUseCase findAllVehiclesUseCase;

    @GetMapping
    @Operation(summary = "Listar vehículos activos", description = "Retorna todos los vehículos activos con placa, marca y tipo. Usado por el móvil para poblar el selector.")
    @ApiResponse(responseCode = "200", description = "Lista retornada exitosamente")
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(findAllVehiclesUseCase.findAllVehicles());
    }

    @GetMapping("/{placa}")
    @Operation(summary = "Consultar vehículo por placa", description = "Retorna id, placa, marca y tipo de un vehículo específico. Usado por el móvil para pre-cargar datos en el formulario de inspección.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado o inactivo")
    })
    public ResponseEntity<VehicleResponse> getVehicleByPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(findAllVehiclesUseCase.findByPlaca(placa));
    }
}
