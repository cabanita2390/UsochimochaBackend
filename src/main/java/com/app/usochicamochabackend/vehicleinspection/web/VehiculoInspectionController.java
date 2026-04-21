package com.app.usochicamochabackend.vehicleinspection.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.vehicleinspection.application.dto.DocumentoVehiculoResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.KilometrajeValidacionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionRequest;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.port.CreateVehiculoInspectionUseCase;
import com.app.usochicamochabackend.vehicleinspection.application.service.VehiculoInspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/v1/vehicle-inspection")
@RequiredArgsConstructor
@Tag(name = "Vehicle Inspection", description = "Endpoints para la Inspección Pre-Operativa de Vehículos")
public class VehiculoInspectionController {

        private final CreateVehiculoInspectionUseCase createVehiculoInspectionUseCase;
        private final VehiculoInspectionService vehiculoInspectionService;

        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
        @Operation(summary = "Registrar una inspección pre-operativa de vehículo", description = "Guarda la inspección completa en 5 tablas: cabecera, mecánico, documentos, elementos y salud.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Inspección guardada exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
                        @ApiResponse(responseCode = "401", description = "No autorizado"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        public ResponseEntity<VehiculoInspectionResponse> createInspection(
                        @RequestBody VehiculoInspectionRequest request,
                        Authentication authentication) throws URISyntaxException {

                // Extraer el inspector autenticado desde el JWT
                UserPrincipal inspector = (UserPrincipal) authentication.getPrincipal();

                VehiculoInspectionResponse saved = createVehiculoInspectionUseCase.create(request, inspector);

                return ResponseEntity
                                .created(new URI("/api/v1/vehicle-inspection/" + saved.idInspeccion()))
                                .body(saved);
        }

        @GetMapping("/documentos/{idVehiculo}")
        @Operation(summary = "Consultar documentos de un vehículo", description = "Retorna las fechas de vencimiento más recientes (SOAT, TECNO, LICENCIA, EXTINTOR) y su estado calculado automáticamente (Vigente / Próximo a Vencer / Vencido).")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Documentos encontrados"),
                        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
        })
        public ResponseEntity<DocumentoVehiculoResponse> getDocumentos(@PathVariable Integer idVehiculo) {
                return ResponseEntity.ok(vehiculoInspectionService.getDocumentos(idVehiculo));
        }

        @GetMapping("/validar-kilometraje")
        @Operation(summary = "Validar kilometraje del inspector", description = "Compara el kilometraje ingresado por el inspector con el último registrado. "
                        +
                        "Retorna alerta=true si el valor ingresado es menor al registrado en la base de datos.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Validación exitosa"),
                        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
        })
        public ResponseEntity<KilometrajeValidacionResponse> validarKilometraje(
                        @RequestParam String placa,
                        @RequestParam Integer kilometraje) {
                return ResponseEntity.ok(vehiculoInspectionService.validarKilometraje(placa, kilometraje));
        }
}
