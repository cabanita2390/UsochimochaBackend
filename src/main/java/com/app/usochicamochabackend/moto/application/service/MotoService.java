package com.app.usochicamochabackend.moto.application.service;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.catalog.infrastructure.entity.UbicacionEntity;
import com.app.usochicamochabackend.catalog.infrastructure.repository.UbicacionRepository;
import com.app.usochicamochabackend.mapper.VehicleMapper;
import com.app.usochicamochabackend.moto.application.dto.*;
import com.app.usochicamochabackend.moto.application.port.MotoCRUDUseCase;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleRequest;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.DocumentacionYElementosEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspDetalleDocumentosEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspDetalleMecanicoEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspPreOperativaEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.DocumentacionYElementosRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleDocumentosRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleMecanicoRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspPreOperativaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotoService implements MotoCRUDUseCase {

    private final VehicleRepository vehicleRepository;
    private final UbicacionRepository ubicacionRepository;
    private final InspPreOperativaRepository inspeccionRepository;
    private final InspDetalleDocumentosRepository detalleDocumentosRepository;
    private final InspDetalleMecanicoRepository detalleMecanicoRepository;
    private final DocumentacionYElementosRepository documentacionRepository;

        /** Retorna las motocicletas activas (tipo = MOTOCICLETA) */
        public List<MotoPlacaResponse> getMotocicletas() {
                return vehicleRepository.findAllByTipoName("MOTOCICLETA")
                                .stream()
                                .map(v -> new MotoPlacaResponse(v.getIdVehiculo(), v.getPlaca()))
                                .toList();
        }

        /** Retorna todas las ubicaciones activas */
        public List<UbicacionResponse> getUbicaciones() {
                return ubicacionRepository.findByActivoTrue()
                                .stream()
                                .map(u -> new UbicacionResponse(u.getId(), u.getNombreUbicacion()))
                                .toList();
        }

        /**
         * Pre-llenado de la app: retorna el estado de los 3 documentos de la moto
         * - Fecha/Imagen: De la TABLA MAESTRA documentacion_y_elementos.
         * - Estado: De la ÚLTIMA INSPECCIÓN (insp_detalle_documentos).
         * - Fallback: Si no hay inspección previa, calcula el estado en tiempo real.
         */
        public List<DocumentoExistenteResponse> getDocumentosByPlaca(String placa) {
                VehicleEntity vehiculo = vehicleRepository.findByPlacaAndActivoTrue(placa)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Vehículo no encontrado: " + placa));

                // Definir los tipos que la APP espera y mapearlos a la Base de Datos
                return java.util.List.of("SOAT", "REVISION_TECNO", "LICENCIA").stream()
                                .map(tipoApp -> {
                                        // Mapeo: App -> DB
                                        String tipoDb = switch (tipoApp) {
                                                case "REVISION_TECNO" -> "TECNOMECANICA";
                                                case "LICENCIA" -> "LICENCIA DE CONDUCCION";
                                                default -> tipoApp;
                                        };

                                        // Buscar datos maestros (fecha, imagen)
                                        var docOpt = documentacionRepository.findLatestByVehiculoAndTipo(
                                                        vehiculo.getIdVehiculo(), tipoDb);

                                        java.time.LocalDate fechaVenc = null;
                                        String fullImagenUrl = null;
                                        String mesyear = null;

                                        if (docOpt.isPresent()) {
                                                DocumentacionYElementosEntity doc = docOpt.get();
                                                fechaVenc = doc.getFechaVencimiento();

                                                if (doc.getImagenUrl() != null && !doc.getImagenUrl().isBlank()) {
                                                        String rawUrl = doc.getImagenUrl().trim();
                                                        if (rawUrl.toLowerCase().startsWith("http")) {
                                                                fullImagenUrl = rawUrl;
                                                        } else {
                                                                // Lógica Universal: Soporta rutas completas (Web) y nombres de archivo (Manual)
                                                                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
                                                                String cleanPath = rawUrl.replace("\\", "/");
                                                                if (cleanPath.startsWith("/")) cleanPath = cleanPath.substring(1);

                                                                if (cleanPath.toLowerCase().startsWith("uploads/")) {
                                                                        // Ya trae la ruta (subido desde la Web)
                                                                        fullImagenUrl = baseUrl + "/" + cleanPath;
                                                                } else {
                                                                        // Es solo el nombre (subido manual a carpeta documents)
                                                                        fullImagenUrl = baseUrl + "/uploads/documents/" + cleanPath;
                                                                }
                                                        }
                                                }

                                                if (fechaVenc != null) {
                                                        mesyear = fechaVenc.getYear() + "-"
                                                                        + String.format("%02d",
                                                                                        fechaVenc.getMonthValue());
                                                }
                                        }

                                        // Lógica IGUAL A VEHÍCULOS: Calcular estado siempre desde la fecha maestra
                                        String estado = calcularEstado(fechaVenc);

                                        return new DocumentoExistenteResponse(
                                                        null, tipoApp, fechaVenc, mesyear, fullImagenUrl,
                                                        vehiculo.getKilometrajeActual(),
                                                        estado);
                                })
                                .toList();
        }

        @Transactional
        public Long saveInspeccion(InspeccionMotoRequest req) {
                log.info("📥 [MotoService] Iniciando guardado de inspección para vehículo ID: {}", req.idVehiculo());

                UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getPrincipal();
                String responsable = userPrincipal.username();

                VehicleEntity vehiculo = vehicleRepository.findById(req.idVehiculo())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Vehículo no encontrado: " + req.idVehiculo()));

                if (req.kilometrajeReportado() != null && req.kilometrajeReportado() > 0) {
                        vehicleRepository.updateKilometrajeWithDate(vehiculo.getIdVehiculo(), req.kilometrajeReportado(), LocalDateTime.now());
                }

                InspPreOperativaEntity inspeccion = InspPreOperativaEntity.builder()
                                .idVehiculo(vehiculo.getIdVehiculo())
                                .fechaRegistro(LocalDateTime.now())
                                .kilometrajeReportado(req.kilometrajeReportado())
                                .estadoVehiculo(req.estadoVehiculo())
                                .idUbicacion(req.idUbicacion())
                                .observacionesFinales(req.observacionesFinales())
                                .loginUser(responsable)
                                .build();

                inspeccionRepository.save(inspeccion);

                // ── 2: insp_detalle_mecanico (Estado Visual + Campos nuevos) ─────────
                detalleMecanicoRepository.save(
                                InspDetalleMecanicoEntity.builder()
                                                .idInspeccion(inspeccion.getIdInspeccion())
                                                .nivelAceite(req.checkNivelAceite())
                                                .estadoLlantas(req.checkEstadoLlantas())
                                                .lucesGeneral(req.checkEstadoLuces())
                                                .estadoVisual(req.estadoVehiculo()) // "Estado general" de la moto
                                                .build());

                // ── 3: insp_detalle_documentos ────────────────────────────────────────
                InspDetalleDocumentosEntity detalleDoc = InspDetalleDocumentosEntity.builder()
                                .idInspeccion(inspeccion.getIdInspeccion())
                                .checkSoat(req.checkSoat())
                                .checkTecno(req.checkTecno())
                                .checkLicencia(req.checkLicencia())
                                .checkExtintor(req.checkExtintor())
                                .build();
                detalleDocumentosRepository.save(detalleDoc);

                return inspeccion.getIdInspeccion();
        }

        private String calcularEstado(java.time.LocalDate fechaVencimiento) {
                if (fechaVencimiento == null)
                        return "Sin Información";

                java.time.YearMonth mesVencimiento = java.time.YearMonth.from(fechaVencimiento);
                java.time.YearMonth mesActual = java.time.YearMonth.now();

                // 1. Si el mes ya pasó -> Vencido
                if (mesVencimiento.isBefore(mesActual)) {
                        return "Vencido";
                }

                // 2. Si el mes de vencimiento es HOY -> Vigente (para no bloquear hoy)
                if (mesVencimiento.equals(mesActual)) {
                        return "Vigente";
                }

                // 3. Si el mes de vencimiento es el SIGUIENTE -> Próximo a Vencer
                if (mesVencimiento.equals(mesActual.plusMonths(1))) {
                        return "Próximo a Vencer";
                }

                // 4. Si es en el futuro
                return "Vigente";
        }
    // --- CRUD Motocicletas ---

    @Override
    public List<VehicleResponse> findAllMotos() {
        return vehicleRepository.findAllByTipoName("MOTOCICLETA").stream()
                .map(VehicleMapper::toResponse)
                .toList();
    }

    @Override
    public VehicleResponse createMoto(VehicleRequest request) {
        // Asegurar que siempre sea tipo MOTOCICLETA (ej: ID 2 o buscar por nombre)
        // Por simplicidad usaremos el ID que venga en el request, pero podemos forzarlo.
        VehicleEntity entity = VehicleEntity.builder()
                .placa(request.placa())
                .idMarca(request.idMarca())
                .idTipoVehiculo(request.idTipoVehiculo()) // Aquí debería ser el ID de Moto
                .kilometrajeActual(request.kilometrajeActual())
                .belongsTo(request.belongsTo())
                .activo(true)
                .build();
        vehicleRepository.save(entity);
        return VehicleMapper.toResponse(entity);
    }

    @Override
    public VehicleResponse updateMoto(Integer id, VehicleRequest request) {
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Moto no encontrada"));
        entity.setPlaca(request.placa());
        entity.setIdMarca(request.idMarca());
        entity.setKilometrajeActual(request.kilometrajeActual());
        entity.setBelongsTo(request.belongsTo());
        entity.setActivo(request.activo());
        vehicleRepository.save(entity);
        return VehicleMapper.toResponse(entity);
    }

    @Override
    public void deleteMoto(Integer id) {
        vehicleRepository.deleteById(id);
    }
}
