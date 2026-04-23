package com.app.usochicamochabackend.moto.web;

import com.app.usochicamochabackend.moto.application.dto.*;
import com.app.usochicamochabackend.moto.application.service.MotoService;
import com.app.usochicamochabackend.moto.infrastructure.entity.InspeccionEntity;
import com.app.usochicamochabackend.moto.infrastructure.entity.VehiculoEntity;
import com.app.usochicamochabackend.moto.infrastructure.repository.MotoInspeccionRepository;
import com.app.usochicamochabackend.moto.infrastructure.repository.VehiculoRepository;
import com.app.usochicamochabackend.moto.infrastructure.repository.TipoVehiculoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/moto")
@RequiredArgsConstructor
@Tag(name = "Moto", description = "Endpoints para inspección y gestión de motocicletas")
public class MotoController {

    private final MotoService motoService;
    private final MotoInspeccionRepository motoInspeccionRepository;
    private final VehiculoRepository vehiculoRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;

    /* ═══════════════ CONSULTAS EXISTENTES ═══════════════ */

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

    @GetMapping("/{placa}/documentos")
    @Operation(summary = "Documentos existentes de una moto", description = "Devuelve documentos + estado de última inspección para el pre-llenado")
    public ResponseEntity<List<DocumentoExistenteResponse>> getDocumentos(@PathVariable String placa) {
        return ResponseEntity.ok(motoService.getDocumentosByPlaca(placa));
    }

    @PostMapping("/inspeccion")
    @Operation(summary = "Guardar inspección pre-operativa", description = "Registra la inspección completa de la motocicleta")
    public ResponseEntity<Long> saveInspeccion(@RequestBody InspeccionMotoRequest request) {
        Long id = motoService.saveInspeccion(request);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/documento/imagen/{fileName:.+}")
    @Operation(summary = "Obtener imagen de documento", description = "Retorna el flujo de bytes de la imagen del documento")
    public ResponseEntity<Resource> getDocumentoImagen(@PathVariable String fileName) {
        try {
            Path path = Paths.get("uploads/documents").resolve(fileName);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }
        } catch (Exception e) {
            // Log error
        }
        return ResponseEntity.notFound().build();
    }

    /* ═══════════════ CRUD MOTOS (Admin Web) ═══════════════ */

    @GetMapping("/all")
    @Operation(summary = "Listar todas las motocicletas (admin)", description = "Retorna todas las motos incluyendo inactivas para el panel admin")
    public ResponseEntity<List<Map<String, Object>>> getAllMotos() {
        List<VehiculoEntity> motos = vehiculoRepository.findActivosByTipo("MOTOCICLETA");
        // Also get inactive ones
        List<VehiculoEntity> all = vehiculoRepository.findAll().stream()
                .filter(v -> v.getTipoVehiculo() != null &&
                        v.getTipoVehiculo().getNombreTipo().equalsIgnoreCase("MOTOCICLETA"))
                .toList();
        List<Map<String, Object>> result = all.stream().map(v -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", v.getId());
            row.put("placa", v.getPlaca());
            row.put("tipoVehiculo", v.getTipoVehiculo() != null ? v.getTipoVehiculo().getNombreTipo() : "N/A");
            row.put("kilometrajeActual", v.getKilometrajeActual());
            row.put("activo", v.getActivo());
            return row;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Crear motocicleta")
    public ResponseEntity<Map<String, Object>> createMoto(@RequestBody Map<String, Object> request) {
        var tipoMoto = tipoVehiculoRepository.findByNombreTipo("MOTOCICLETA")
                .orElseThrow(() -> new RuntimeException("Tipo MOTOCICLETA no encontrado"));
        VehiculoEntity entity = VehiculoEntity.builder()
                .placa((String) request.get("placa"))
                .tipoVehiculo(tipoMoto)
                .kilometrajeActual(request.get("kilometrajeActual") != null ? ((Number) request.get("kilometrajeActual")).intValue() : 0)
                .activo(true)
                .build();
        VehiculoEntity saved = vehiculoRepository.save(entity);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", saved.getId());
        row.put("placa", saved.getPlaca());
        row.put("tipoVehiculo", "MOTOCICLETA");
        row.put("kilometrajeActual", saved.getKilometrajeActual());
        row.put("activo", saved.getActivo());
        return ResponseEntity.status(201).body(row);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar motocicleta")
    public ResponseEntity<Map<String, Object>> updateMoto(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        VehiculoEntity entity = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Moto no encontrada: " + id));
        if (request.containsKey("placa")) entity.setPlaca((String) request.get("placa"));
        if (request.containsKey("kilometrajeActual")) entity.setKilometrajeActual(((Number) request.get("kilometrajeActual")).intValue());
        if (request.containsKey("activo")) entity.setActivo((Boolean) request.get("activo"));
        VehiculoEntity saved = vehiculoRepository.save(entity);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", saved.getId());
        row.put("placa", saved.getPlaca());
        row.put("tipoVehiculo", saved.getTipoVehiculo() != null ? saved.getTipoVehiculo().getNombreTipo() : "N/A");
        row.put("kilometrajeActual", saved.getKilometrajeActual());
        row.put("activo", saved.getActivo());
        return ResponseEntity.ok(row);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar motocicleta (soft delete)")
    public ResponseEntity<Void> deleteMoto(@PathVariable Integer id) {
        VehiculoEntity entity = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Moto no encontrada: " + id));
        entity.setActivo(false);
        vehiculoRepository.save(entity);
        return ResponseEntity.noContent().build();
    }

    /* ═══════════════ INSPECCIONES MOTOS (Listado Web) ═══════════════ */

    @GetMapping("/inspecciones")
    @Operation(summary = "Listar inspecciones de motos paginadas", description = "Retorna inspecciones pre-operativas de motocicletas")
    public ResponseEntity<Page<Map<String, Object>>> listMotoInspections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<InspeccionEntity> result = motoInspeccionRepository
                .findAllByTipoVehiculo("MOTOCICLETA", PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaRegistro")));
        Page<Map<String, Object>> mapped = result.map(insp -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("idInspeccion", insp.getId());
            row.put("fechaRegistro", insp.getFechaRegistro());
            row.put("placa", insp.getVehiculo() != null ? insp.getVehiculo().getPlaca() : "N/A");
            row.put("ubicacion", insp.getUbicacion() != null ? insp.getUbicacion().getNombreUbicacion() : "N/A");
            row.put("loginUser", insp.getLoginUser());
            row.put("kilometrajeReportado", insp.getKilometrajeReportado());
            row.put("estadoVehiculo", insp.getEstadoVehiculo());
            row.put("observacionesFinales", insp.getObservacionesFinales());
            return row;
        });
        return ResponseEntity.ok(mapped);
    }
}