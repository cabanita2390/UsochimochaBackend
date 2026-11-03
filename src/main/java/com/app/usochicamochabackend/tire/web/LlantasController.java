package com.app.usochicamochabackend.tire.web;

import com.app.usochicamochabackend.tire.application.dto.AsignacionLlantasRequest;
import com.app.usochicamochabackend.tire.application.dto.ConfigLlantasActivoResponse;
import com.app.usochicamochabackend.tire.application.dto.PerfilLlantasRequest;
import com.app.usochicamochabackend.tire.application.dto.PerfilLlantasResponse;
import com.app.usochicamochabackend.tire.application.port.GestionarPerfilLlantasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Perfiles de llantas (posiciones + rango de presión en PSI) y su asignación a vehículos, motos y
 * máquinas. Lectura para cualquier autenticado (el móvil descarga las asignaciones); escritura para
 * ADMIN y SUPERVISOR_OPERATIVO (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/tire")
@RequiredArgsConstructor
@Tag(name = "Presión de llantas", description = "Perfiles de llantas y asignación por activo")
public class LlantasController {

    private final GestionarPerfilLlantasUseCase gestionarPerfilLlantasUseCase;

    @GetMapping("/perfiles")
    public ResponseEntity<List<PerfilLlantasResponse>> listarPerfiles() {
        return ResponseEntity.ok(gestionarPerfilLlantasUseCase.listarPerfiles());
    }

    @PostMapping("/perfiles")
    public ResponseEntity<PerfilLlantasResponse> crearPerfil(@RequestBody PerfilLlantasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gestionarPerfilLlantasUseCase.crearPerfil(request));
    }

    @PutMapping("/perfiles/{id}")
    public ResponseEntity<PerfilLlantasResponse> actualizarPerfil(@PathVariable Long id,
                                                                  @RequestBody PerfilLlantasRequest request) {
        return ResponseEntity.ok(gestionarPerfilLlantasUseCase.actualizarPerfil(id, request));
    }

    @DeleteMapping("/perfiles/{id}")
    public ResponseEntity<Void> desactivarPerfil(@PathVariable Long id) {
        gestionarPerfilLlantasUseCase.desactivarPerfil(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Perfil vigente de cada activo",
            description = "Lo usa el móvil para cachear qué llantas pedir y con qué rango alertar sin conexión.")
    @GetMapping("/asignaciones")
    public ResponseEntity<List<ConfigLlantasActivoResponse>> listarAsignaciones() {
        return ResponseEntity.ok(gestionarPerfilLlantasUseCase.listarAsignaciones());
    }

    @Operation(summary = "Asignar perfil a un vehículo o moto", description = "perfilId null quita la asignación (204).")
    @PutMapping("/asignaciones/vehicle/{vehicleId}")
    public ResponseEntity<ConfigLlantasActivoResponse> asignarVehiculo(@PathVariable Integer vehicleId,
                                                                       @RequestBody AsignacionLlantasRequest request) {
        return okOrNoContent(gestionarPerfilLlantasUseCase.asignarVehiculo(vehicleId, request));
    }

    @Operation(summary = "Asignar perfil a una máquina", description = "perfilId null quita la asignación (204).")
    @PutMapping("/asignaciones/machine/{machineId}")
    public ResponseEntity<ConfigLlantasActivoResponse> asignarMaquina(@PathVariable Long machineId,
                                                                      @RequestBody AsignacionLlantasRequest request) {
        return okOrNoContent(gestionarPerfilLlantasUseCase.asignarMaquina(machineId, request));
    }

    private static ResponseEntity<ConfigLlantasActivoResponse> okOrNoContent(ConfigLlantasActivoResponse body) {
        return body != null ? ResponseEntity.ok(body) : ResponseEntity.noContent().build();
    }
}
