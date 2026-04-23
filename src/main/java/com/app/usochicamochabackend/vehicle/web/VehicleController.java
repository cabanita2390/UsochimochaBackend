package com.app.usochicamochabackend.vehicle.web;

import com.app.usochicamochabackend.vehicle.application.dto.*;
import com.app.usochicamochabackend.vehicle.application.port.FindAllVehiclesUseCase;
import com.app.usochicamochabackend.vehicle.application.service.VehicleService;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.DocumentacionYElementosEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.DocumentacionYElementosRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicle")
@RequiredArgsConstructor
@Tag(name = "Vehicle", description = "Endpoints para vehículos")
public class VehicleController {

    private final VehicleService vehicleService;
    private final DocumentacionYElementosRepository documentacionRepository;

    /* ═══════════════ VEHÍCULOS ═══════════════ */

    @GetMapping
    @Operation(summary = "Listar vehículos activos", description = "Retorna todos los vehículos activos con placa, marca y tipo. Usado por el móvil para poblar el selector.")
    @ApiResponse(responseCode = "200", description = "Lista retornada exitosamente")
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.findAllVehicles());
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los vehículos (admin)", description = "Retorna todos los vehículos incluyendo inactivos, con IDs de catálogo.")
    public ResponseEntity<List<VehicleFullResponse>> getAllVehiclesFull() {
        return ResponseEntity.ok(vehicleService.findAllVehiclesFull());
    }

    @GetMapping("/{placa}")
    @Operation(summary = "Consultar vehículo por placa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Vehículo no encontrado o inactivo")
    })
    public ResponseEntity<VehicleResponse> getVehicleByPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(vehicleService.findByPlaca(placa));
    }

    @PostMapping
    @Operation(summary = "Crear vehículo")
    @ApiResponse(responseCode = "201", description = "Vehículo creado")
    public ResponseEntity<VehicleFullResponse> createVehicle(@RequestBody VehicleRequest request) {
        return ResponseEntity.status(201).body(vehicleService.createVehicle(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar vehículo")
    public ResponseEntity<VehicleFullResponse> updateVehicle(@PathVariable Long id, @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar vehículo (soft delete)")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    /* ═══════════════ CATÁLOGO: MARCAS ═══════════════ */

    @GetMapping("/catalogo/marcas")
    @Operation(summary = "Listar todas las marcas/modelos")
    public ResponseEntity<List<CatalogoResponse>> getAllMarcas() {
        return ResponseEntity.ok(vehicleService.findAllMarcas());
    }

    @PostMapping("/catalogo/marcas")
    @Operation(summary = "Crear marca/modelo")
    public ResponseEntity<CatalogoResponse> createMarca(@RequestBody CatalogoRequest request) {
        return ResponseEntity.status(201).body(vehicleService.createMarca(request));
    }

    @PutMapping("/catalogo/marcas/{id}")
    @Operation(summary = "Actualizar marca/modelo")
    public ResponseEntity<CatalogoResponse> updateMarca(@PathVariable Integer id, @RequestBody CatalogoRequest request) {
        return ResponseEntity.ok(vehicleService.updateMarca(id, request));
    }

    @DeleteMapping("/catalogo/marcas/{id}")
    @Operation(summary = "Eliminar marca/modelo")
    public ResponseEntity<Void> deleteMarca(@PathVariable Integer id) {
        vehicleService.deleteMarca(id);
        return ResponseEntity.noContent().build();
    }

    /* ═══════════════ CATÁLOGO: TIPOS DE VEHÍCULO ═══════════════ */

    @GetMapping("/catalogo/tipos")
    @Operation(summary = "Listar tipos de vehículo")
    public ResponseEntity<List<CatalogoResponse>> getAllTipos() {
        return ResponseEntity.ok(vehicleService.findAllTipos());
    }

    @PostMapping("/catalogo/tipos")
    @Operation(summary = "Crear tipo de vehículo")
    public ResponseEntity<CatalogoResponse> createTipo(@RequestBody CatalogoRequest request) {
        return ResponseEntity.status(201).body(vehicleService.createTipo(request));
    }

    @PutMapping("/catalogo/tipos/{id}")
    @Operation(summary = "Actualizar tipo de vehículo")
    public ResponseEntity<CatalogoResponse> updateTipo(@PathVariable Integer id, @RequestBody CatalogoRequest request) {
        return ResponseEntity.ok(vehicleService.updateTipo(id, request));
    }

    @DeleteMapping("/catalogo/tipos/{id}")
    @Operation(summary = "Desactivar tipo de vehículo")
    public ResponseEntity<Void> deleteTipo(@PathVariable Integer id) {
        vehicleService.deleteTipo(id);
        return ResponseEntity.noContent().build();
    }

    /* ═══════════════ CATÁLOGO: UBICACIONES ═══════════════ */

    @GetMapping("/catalogo/ubicaciones")
    @Operation(summary = "Listar ubicaciones")
    public ResponseEntity<List<CatalogoResponse>> getAllUbicaciones() {
        return ResponseEntity.ok(vehicleService.findAllUbicaciones());
    }

    @PostMapping("/catalogo/ubicaciones")
    @Operation(summary = "Crear ubicación")
    public ResponseEntity<CatalogoResponse> createUbicacion(@RequestBody CatalogoRequest request) {
        return ResponseEntity.status(201).body(vehicleService.createUbicacion(request));
    }

    @PutMapping("/catalogo/ubicaciones/{id}")
    @Operation(summary = "Actualizar ubicación")
    public ResponseEntity<CatalogoResponse> updateUbicacion(@PathVariable Integer id, @RequestBody CatalogoRequest request) {
        return ResponseEntity.ok(vehicleService.updateUbicacion(id, request));
    }

    @DeleteMapping("/catalogo/ubicaciones/{id}")
    @Operation(summary = "Desactivar ubicación")
    public ResponseEntity<Void> deleteUbicacion(@PathVariable Integer id) {
        vehicleService.deleteUbicacion(id);
        return ResponseEntity.noContent().build();
    }

    /* ═══════════════ CATÁLOGO: REFERENCIAS DE ACEITE ═══════════════ */

    @GetMapping("/catalogo/aceites")
    @Operation(summary = "Listar referencias de aceite")
    public ResponseEntity<List<CatalogoResponse>> getAllAceites() {
        return ResponseEntity.ok(vehicleService.findAllAceites());
    }

    @PostMapping("/catalogo/aceites")
    @Operation(summary = "Crear referencia de aceite")
    public ResponseEntity<CatalogoResponse> createAceite(@RequestBody CatalogoRequest request) {
        return ResponseEntity.status(201).body(vehicleService.createAceite(request));
    }

    @PutMapping("/catalogo/aceites/{id}")
    @Operation(summary = "Actualizar referencia de aceite")
    public ResponseEntity<CatalogoResponse> updateAceite(@PathVariable Integer id, @RequestBody CatalogoRequest request) {
        return ResponseEntity.ok(vehicleService.updateAceite(id, request));
    }

    @DeleteMapping("/catalogo/aceites/{id}")
    @Operation(summary = "Desactivar referencia de aceite")
    public ResponseEntity<Void> deleteAceite(@PathVariable Integer id) {
        vehicleService.deleteAceite(id);
        return ResponseEntity.noContent().build();
    }

    /* ═══════════════ DOCUMENTACIÓN DE VEHÍCULOS ═══════════════ */

    private Map<String, Object> toDocMap(DocumentacionYElementosEntity d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getIdDocumento());
        m.put("idVehiculo", d.getIdVehiculo());
        m.put("placa", d.getVehiculo() != null ? d.getVehiculo().getPlaca() : "N/A");
        m.put("tipoDocumento", d.getTipoDocumento());
        m.put("fechaVencimiento", d.getFechaVencimiento());
        m.put("imagenUrl", d.getImagenUrl());
        m.put("estadoDatos", d.getEstadoDatos());
        m.put("activo", d.getActivo());
        // Calcular estado visual
        if (d.getFechaVencimiento() != null) {
            LocalDate hoy = LocalDate.now();
            LocalDate dosM = hoy.plusMonths(2);
            if (d.getFechaVencimiento().isBefore(hoy)) m.put("estadoCalc", "Vencido");
            else if (d.getFechaVencimiento().isBefore(dosM)) m.put("estadoCalc", "Próximo a Vencer");
            else m.put("estadoCalc", "Vigente");
        }
        return m;
    }

    @GetMapping("/documentos")
    @Operation(summary = "Listar todos los documentos de vehículos")
    public ResponseEntity<List<Map<String, Object>>> getAllDocumentos() {
        return ResponseEntity.ok(
                documentacionRepository.findByActivoTrueOrderByIdVehiculoAscTipoDocumentoAsc()
                        .stream().map(this::toDocMap).toList()
        );
    }

    @GetMapping("/documentos/{idVehiculo}")
    @Operation(summary = "Listar documentos de un vehículo")
    public ResponseEntity<List<Map<String, Object>>> getDocumentosByVehiculo(@PathVariable Integer idVehiculo) {
        return ResponseEntity.ok(
                documentacionRepository.findByIdVehiculoAndActivoTrueOrderByTipoDocumento(idVehiculo)
                        .stream().map(this::toDocMap).toList()
        );
    }

    @PostMapping("/documentos")
    @Operation(summary = "Crear documento de vehículo")
    public ResponseEntity<Map<String, Object>> createDocumento(@RequestBody Map<String, Object> req) {
        DocumentacionYElementosEntity entity = new DocumentacionYElementosEntity();
        entity.setIdVehiculo(((Number) req.get("idVehiculo")).intValue());
        entity.setTipoDocumento((String) req.get("tipoDocumento"));
        entity.setFechaVencimiento(LocalDate.parse((String) req.get("fechaVencimiento")));
        entity.setImagenUrl((String) req.get("imagenUrl"));
        entity.setActivo(true);
        DocumentacionYElementosEntity saved = documentacionRepository.save(entity);
        return ResponseEntity.status(201).body(toDocMap(saved));
    }

    @PutMapping("/documentos/{id}")
    @Operation(summary = "Actualizar documento de vehículo")
    public ResponseEntity<Map<String, Object>> updateDocumento(@PathVariable Integer id, @RequestBody Map<String, Object> req) {
        DocumentacionYElementosEntity entity = documentacionRepository.findById(id)
                .orElseThrow(() -> new com.app.usochicamochabackend.exception.ResourceNotFoundException("Documento no encontrado: " + id));
        if (req.containsKey("tipoDocumento")) entity.setTipoDocumento((String) req.get("tipoDocumento"));
        if (req.containsKey("fechaVencimiento")) entity.setFechaVencimiento(LocalDate.parse((String) req.get("fechaVencimiento")));
        if (req.containsKey("imagenUrl")) entity.setImagenUrl((String) req.get("imagenUrl"));
        DocumentacionYElementosEntity saved = documentacionRepository.save(entity);
        return ResponseEntity.ok(toDocMap(saved));
    }

    @DeleteMapping("/documentos/{id}")
    @Operation(summary = "Desactivar documento de vehículo")
    public ResponseEntity<Void> deleteDocumento(@PathVariable Integer id) {
        DocumentacionYElementosEntity entity = documentacionRepository.findById(id)
                .orElseThrow(() -> new com.app.usochicamochabackend.exception.ResourceNotFoundException("Documento no encontrado: " + id));
        entity.setActivo(false);
        documentacionRepository.save(entity);
        return ResponseEntity.noContent().build();
    }
}
