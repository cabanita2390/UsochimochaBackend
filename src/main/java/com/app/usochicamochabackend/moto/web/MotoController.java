package com.app.usochicamochabackend.moto.web;

import com.app.usochicamochabackend.moto.application.dto.*;
import com.app.usochicamochabackend.moto.application.service.MotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moto")
@RequiredArgsConstructor
@Tag(name = "Moto", description = "Endpoints para inspección de motocicletas")
public class MotoController {

    private final MotoService motoService;

    @GetMapping("/placas")
    @Operation(summary = "Obtener motocicletas activas", description = "Retorna la lista de placas de motocicletas activas en la BD")
    public ResponseEntity<List<MotoPlacaResponse>> getMotocicletas() {
        return ResponseEntity.ok(motoService.getMotocicletas());
    }

    @GetMapping("/ubicaciones")
    @Operation(summary = "Obtener ubicaciones activas", description = "Retorna la lista de ubicaciones activas en la BD")
    public ResponseEntity<List<UbicacionResponse>> getUbicaciones() {
        return ResponseEntity.ok(motoService.getUbicaciones());
    }

    @PostMapping("/placas")
    @Operation(summary = "Registrar nueva placa", description = "Crea un registro de vehículo mínimo para una nueva motocicleta")
    public ResponseEntity<MotoPlacaResponse> registrarPlaca(@RequestBody String placa) {
        return ResponseEntity.ok(motoService.registrarNuevaPlaca(placa));
    }

    @GetMapping("/{placa}/documentos")
    @Operation(summary = "Documentos existentes de una moto", description = "Pre-fill: devuelve los documentos ya registrados para la placa indicada")
    public ResponseEntity<List<DocumentoExistenteResponse>> getDocumentos(@PathVariable String placa) {
        return ResponseEntity.ok(motoService.getDocumentosByPlaca(placa));
    }

    @PostMapping("/inspeccion")
    @Operation(summary = "Guardar inspección pre-operativa", description = "Registra la inspección completa de la motocicleta")
    public ResponseEntity<Long> saveInspeccion(@RequestBody InspeccionMotoRequest request) {
        Long id = motoService.saveInspeccion(request);
        return ResponseEntity.ok(id);
    }
}
