package com.app.usochicamochabackend.moto.web;

import com.app.usochicamochabackend.moto.application.dto.*;
import com.app.usochicamochabackend.moto.application.service.MotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @GetMapping("/documento/imagen/{fileName}")
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
}