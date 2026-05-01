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

import com.app.usochicamochabackend.vehicle.application.dto.VehicleCreateRequest;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleUpdateRequest;
import com.app.usochicamochabackend.vehicle.application.port.CreateVehicleUseCase;
import com.app.usochicamochabackend.vehicle.application.port.DeleteVehicleUseCase;
import com.app.usochicamochabackend.vehicle.application.port.UpdateVehicleUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/vehicle")
@RequiredArgsConstructor
@Tag(name = "Vehicle", description = "Endpoints para vehículos")
public class VehicleController {

    private final FindAllVehiclesUseCase findAllVehiclesUseCase;
    private final CreateVehicleUseCase createVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;

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

    @PostMapping
    @Operation(summary = "Crear vehículo", description = "Crea un nuevo vehículo en el sistema.")
    @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente")
    public ResponseEntity<VehicleResponse> createVehicle(@Valid @RequestBody VehicleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createVehicleUseCase.createVehicle(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar vehículo", description = "Actualiza la información de un vehículo existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehículo actualizado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<VehicleResponse> updateVehicle(@PathVariable Integer id, @Valid @RequestBody VehicleUpdateRequest request) {
        return ResponseEntity.ok(updateVehicleUseCase.updateVehicle(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar vehículo", description = "Elimina (borrado físico o lógico dependiendo de DB) un vehículo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vehículo eliminado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    public ResponseEntity<Void> deleteVehicle(@PathVariable Integer id) {
        deleteVehicleUseCase.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
