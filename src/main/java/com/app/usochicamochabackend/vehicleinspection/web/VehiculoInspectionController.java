package com.app.usochicamochabackend.vehicleinspection.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.vehicleinspection.application.dto.DocumentoVehiculoResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.KilometrajeValidacionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehicleInspectionReportDTO;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionRequest;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.port.CreateVehiculoInspectionUseCase;
import com.app.usochicamochabackend.vehicleinspection.application.port.GetVehicleInspectionsUseCase;
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
@Tag(
                name = "Vehicle Inspection",
                description = "Inspección **preoperativa completa** para vehículos de campo (livianos/pesados): registro en varias tablas "
                                + "detalladas. Las **motocicletas** usan el flujo simplificado `POST /api/v1/moto/inspeccion`. "
                                + "Listados por tipo de vehículo: `GET .../reports/{typeId}` (p. ej. `typeId=2` para AUTOMOVIL en datos semilla).")
public class VehiculoInspectionController {

        private final CreateVehiculoInspectionUseCase createVehiculoInspectionUseCase;
        private final GetVehicleInspectionsUseCase getVehicleInspectionsUseCase;
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

        @PostMapping("/documentos")
        @Operation(
                        summary = "Actualizar documentos (vigencias) vía inspección",
                        description = "Alias funcional de actualización de vigencias; en despliegues recientes el panel admin puede usar "
                                        + "`POST /api/v1/admin/documents` con el mismo propósito.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Documento guardado"),
                        @ApiResponse(responseCode = "401", description = "No autorizado"),
                        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes")
        })
        public ResponseEntity<Void> updateDocumento(@RequestBody com.app.usochicamochabackend.vehicleinspection.application.dto.VehicleDocumentRequest request) {
                vehiculoInspectionService.saveDocument(request);
                return ResponseEntity.ok().build();
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

        @GetMapping("/reports/{typeId}")
        @Operation(
                        summary = "Reportes de inspección por tipo de vehículo",
                        description = "Listado completo de inspecciones para vehículos cuyo `id_tipo_vehiculo` coincide con `typeId` (catálogo `cat_tipos_vehiculo`). "
                                        + "En datos semilla habituales: 1 = MOTOCICLETA, 2 = AUTOMOVIL, 3 = CAMION, 4 = PICKUP. "
                                        + "La web admin usa `typeId=2` para el listado de inspecciones de livianos.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de reportes (`VehicleInspectionReportDTO`)"),
                        @ApiResponse(responseCode = "401", description = "No autorizado")
        })
        public ResponseEntity<java.util.List<VehicleInspectionReportDTO>> getReportsByType(@PathVariable Integer typeId) {
                return ResponseEntity.ok(getVehicleInspectionsUseCase.getInspectionsByType(typeId));
        }
}
