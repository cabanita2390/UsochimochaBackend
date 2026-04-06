package com.app.usochicamochabackend.moto.application.service;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.moto.application.dto.*;
import com.app.usochicamochabackend.moto.infrastructure.entity.*;
import com.app.usochicamochabackend.moto.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MotoService {

        private final VehiculoRepository vehiculoRepository;
        private final UbicacionRepository ubicacionRepository;
        private final MotoInspeccionRepository inspeccionRepository;
        private final InspDetalleDocumentosRepository detalleDocumentosRepository;
        private final InspDetalleMecanicoRepository detalleMecanicoRepository;
        private final DocumentacionRepository documentacionRepository;

        /** Retorna las motocicletas activas (tipo = MOTOCICLETA) */
        public List<MotoPlacaResponse> getMotocicletas() {
                return vehiculoRepository.findActivosByTipo("MOTOCICLETA")
                                .stream()
                                .map(v -> new MotoPlacaResponse(v.getId(), v.getPlaca()))
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
                VehiculoEntity vehiculo = vehiculoRepository.findByPlacaAndActivoTrue(placa)
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
                                                        vehiculo.getId(), tipoDb);

                                        java.time.LocalDate fechaVenc = null;
                                        String fullImagenUrl = null;
                                        String mesyear = null;

                                        if (docOpt.isPresent()) {
                                                DocumentacionEntity doc = docOpt.get();
                                                fechaVenc = doc.getFechaVencimiento();

                                                if (doc.getImagenUrl() != null && !doc.getImagenUrl().isBlank()) {
                                                        fullImagenUrl = "/api/v1/moto/documento/imagen/"
                                                                        + doc.getImagenUrl();
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

                VehiculoEntity vehiculo = vehiculoRepository.findById(req.idVehiculo())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Vehículo no encontrado: " + req.idVehiculo()));

                if (req.kilometrajeReportado() != null && req.kilometrajeReportado() > 0) {
                        vehiculoRepository.updateKilometraje(vehiculo.getId(), req.kilometrajeReportado(), LocalDateTime.now());
                }

                InspeccionEntity inspeccion = InspeccionEntity.builder()
                                .vehiculo(vehiculo)
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
                                                .idInspeccion(inspeccion.getId())
                                                .nivelAceite(req.checkNivelAceite())
                                                .estadoLlantas(req.checkEstadoLlantas())
                                                .lucesGeneral(req.checkEstadoLuces())
                                                .estadoVisual(req.estadoVehiculo()) // "Estado general" de la moto
                                                .build());

                // ── 3: insp_detalle_documentos ────────────────────────────────────────
                InspDetalleDocumentosEntity detalleDoc = InspDetalleDocumentosEntity.builder()
                                .idInspeccion(inspeccion.getId())
                                .checkSoat(req.checkSoat())
                                .checkTecno(req.checkTecno())
                                .checkLicencia(req.checkLicencia())
                                .checkExtintor(req.checkExtintor())
                                .build();
                detalleDocumentosRepository.save(detalleDoc);

                return inspeccion.getId();
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
}
