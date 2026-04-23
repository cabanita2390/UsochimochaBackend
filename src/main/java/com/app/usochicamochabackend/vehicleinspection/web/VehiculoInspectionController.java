package com.app.usochicamochabackend.vehicleinspection.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.vehicleinspection.application.dto.DocumentoVehiculoResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.KilometrajeValidacionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionRequest;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.port.CreateVehiculoInspectionUseCase;
import com.app.usochicamochabackend.vehicleinspection.application.service.VehiculoInspectionService;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspPreOperativaEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspPreOperativaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicle-inspection")
@RequiredArgsConstructor
@Tag(name = "Vehicle Inspection", description = "Endpoints para la Inspección Pre-Operativa de Vehículos")
public class VehiculoInspectionController {

        private final CreateVehiculoInspectionUseCase createVehiculoInspectionUseCase;
        private final VehiculoInspectionService vehiculoInspectionService;
        private final InspPreOperativaRepository inspPreOperativaRepository;
        private final com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleMecanicoRepository detalleMecanicoRepository;
        @org.springframework.beans.factory.annotation.Qualifier("vehicleInspDetalleDocumentosRepository")
        private final com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleDocumentosRepository detalleDocumentosRepository;
        private final com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleElementosRepository detalleElementosRepository;
        private final com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleSaludRepository detalleSaludRepository;

        @GetMapping
        @Operation(summary = "Listar inspecciones paginadas", description = "Retorna inspecciones pre-operativas con paginación para la webapp.")
        public ResponseEntity<Page<Map<String, Object>>> listInspections(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                Page<InspPreOperativaEntity> result = inspPreOperativaRepository
                                .findAllWithVehiculo(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaRegistro")));
                Page<Map<String, Object>> mapped = result.map(insp -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("idInspeccion", insp.getIdInspeccion());
                        row.put("fechaRegistro", insp.getFechaRegistro());
                        row.put("placa", insp.getVehiculo() != null ? insp.getVehiculo().getPlaca() : "N/A");
                        row.put("tipoVehiculo", insp.getVehiculo() != null && insp.getVehiculo().getTipoVehiculo() != null
                                        ? insp.getVehiculo().getTipoVehiculo().getNombreTipo() : "N/A");
                        row.put("loginUser", insp.getLoginUser());
                        row.put("kilometrajeReportado", insp.getKilometrajeReportado());
                        row.put("aprobadoRuta", insp.getAprobadoRuta());
                        row.put("observacionesFinales", insp.getObservacionesFinales());
                        return row;
                });
                return ResponseEntity.ok(mapped);
        }

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

        @GetMapping("/{id}")
        @Operation(summary = "Detalle completo de inspección", description = "Retorna la cabecera + mecánico + documentos + elementos + salud para una inspección.")
        public ResponseEntity<Map<String, Object>> getInspectionDetail(@PathVariable Long id) {
                var insp = inspPreOperativaRepository.findById(id)
                                .orElseThrow(() -> new com.app.usochicamochabackend.exception.ResourceNotFoundException("Inspección no encontrada: " + id));

                Map<String, Object> detail = new LinkedHashMap<>();

                // Cabecera
                detail.put("idInspeccion", insp.getIdInspeccion());
                detail.put("fechaRegistro", insp.getFechaRegistro());
                detail.put("placa", insp.getVehiculo() != null ? insp.getVehiculo().getPlaca() : "N/A");
                detail.put("tipoVehiculo", insp.getVehiculo() != null && insp.getVehiculo().getTipoVehiculo() != null
                                ? insp.getVehiculo().getTipoVehiculo().getNombreTipo() : "N/A");
                detail.put("loginUser", insp.getLoginUser());
                detail.put("kilometrajeReportado", insp.getKilometrajeReportado());
                detail.put("aprobadoRuta", insp.getAprobadoRuta());
                detail.put("observacionesFinales", insp.getObservacionesFinales());

                // Mecánico
                detalleMecanicoRepository.findByIdInspeccion(id).ifPresent(m -> {
                        Map<String, Object> mec = new LinkedHashMap<>();
                        mec.put("nivelAceite", m.getNivelAceite());
                        mec.put("nivelRefrigerante", m.getNivelRefrigerante());
                        mec.put("nivelFrenos", m.getNivelFrenos());
                        mec.put("estadoLlantas", m.getEstadoLlantas());
                        mec.put("lucesGeneral", m.getLucesGeneral());
                        mec.put("estadoVisual", m.getEstadoVisual());
                        mec.put("limpiezaGeneral", m.getLimpiezaGeneral());
                        detail.put("mecanico", mec);
                });

                // Documentos
                detalleDocumentosRepository.findByIdInspeccion(id).ifPresent(d -> {
                        Map<String, Object> doc = new LinkedHashMap<>();
                        doc.put("checkSoat", d.getCheckSoat());
                        doc.put("checkTecno", d.getCheckTecno());
                        doc.put("checkLicencia", d.getCheckLicencia());
                        doc.put("checkExtintor", d.getCheckExtintor());
                        detail.put("documentos", doc);
                });

                // Elementos
                detalleElementosRepository.findByIdInspeccion(id).ifPresent(e -> {
                        Map<String, Object> elem = new LinkedHashMap<>();
                        elem.put("tieneBotiquin", e.getTieneBotiquin());
                        elem.put("tieneSeñalizacion", e.getTieneSeñalizacion());
                        elem.put("tieneLineasEmergencia", e.getTieneLineasEmergencia());
                        elem.put("tieneLlantaRepuesto", e.getTieneLlantaRepuesto());
                        elem.put("tieneGatoHidraulico", e.getTieneGatoHidraulico());
                        detail.put("elementos", elem);
                });

                // Salud
                detalleSaludRepository.findByIdInspeccion(id).ifPresent(s -> {
                        Map<String, Object> salud = new LinkedHashMap<>();
                        salud.put("saludFisica", s.getSaludFisica());
                        salud.put("saludMental", s.getSaludMental());
                        salud.put("sobrio", s.getSobrio());
                        salud.put("medicamentos", s.getMedicamentos());
                        salud.put("condicionParaConducir", s.getCondicionParaConducir());
                        salud.put("conscienteResponsabilidad", s.getConscienteResponsabilidad());
                        detail.put("salud", salud);
                });

                return ResponseEntity.ok(detail);
        }
}
