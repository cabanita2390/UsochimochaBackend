package com.app.usochicamochabackend.catalog.web;

import com.app.usochicamochabackend.catalog.application.dto.CatalogDTO;
import com.app.usochicamochabackend.catalog.application.service.AreaService;
import com.app.usochicamochabackend.catalog.application.service.TipoVehiculoService;
import com.app.usochicamochabackend.catalog.application.service.UbicacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Endpoints para administración de catálogos (Áreas, Ubicaciones)")
public class CatalogController {

    private final AreaService areaService;
    private final UbicacionService ubicacionService;
    private final TipoVehiculoService tipoVehiculoService;

    // --- Areas ---
    @GetMapping("/area")
    public ResponseEntity<List<CatalogDTO>> getAreas() { return ResponseEntity.ok(areaService.findAll()); }

    @PostMapping("/area")
    public ResponseEntity<CatalogDTO> createArea(@RequestBody CatalogDTO dto) { return ResponseEntity.ok(areaService.create(dto)); }

    @PutMapping("/area/{id}")
    public ResponseEntity<CatalogDTO> updateArea(@PathVariable Integer id, @RequestBody CatalogDTO dto) { return ResponseEntity.ok(areaService.update(id, dto)); }

    @DeleteMapping("/area/{id}")
    public ResponseEntity<Void> deleteArea(@PathVariable Integer id) { areaService.delete(id); return ResponseEntity.noContent().build(); }

    // --- Ubicaciones ---
    @GetMapping("/ubicacion")
    public ResponseEntity<List<CatalogDTO>> getUbicaciones() { return ResponseEntity.ok(ubicacionService.findAll()); }

    @PostMapping("/ubicacion")
    public ResponseEntity<CatalogDTO> createUbicacion(@RequestBody CatalogDTO dto) { return ResponseEntity.ok(ubicacionService.create(dto)); }

    @PutMapping("/ubicacion/{id}")
    public ResponseEntity<CatalogDTO> updateUbicacion(@PathVariable Integer id, @RequestBody CatalogDTO dto) { return ResponseEntity.ok(ubicacionService.update(id, dto)); }

    @DeleteMapping("/ubicacion/{id}")
    public ResponseEntity<Void> deleteUbicacion(@PathVariable Integer id) { ubicacionService.delete(id); return ResponseEntity.noContent().build(); }

    // --- Tipos de Vehiculo ---
    @GetMapping("/tipo-vehiculo")
    public ResponseEntity<List<CatalogDTO>> getTiposVehiculo() { return ResponseEntity.ok(tipoVehiculoService.findAll()); }

    @PostMapping("/tipo-vehiculo")
    public ResponseEntity<CatalogDTO> createTipoVehiculo(@RequestBody CatalogDTO dto) { return ResponseEntity.ok(tipoVehiculoService.create(dto)); }

    @PutMapping("/tipo-vehiculo/{id}")
    public ResponseEntity<CatalogDTO> updateTipoVehiculo(@PathVariable Integer id, @RequestBody CatalogDTO dto) { return ResponseEntity.ok(tipoVehiculoService.update(id, dto)); }

    @DeleteMapping("/tipo-vehiculo/{id}")
    public ResponseEntity<Void> deleteTipoVehiculo(@PathVariable Integer id) { tipoVehiculoService.delete(id); return ResponseEntity.noContent().build(); }
}
