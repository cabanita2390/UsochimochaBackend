package com.app.usochicamochabackend.vehicleinspection.application.service;

import java.util.List;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.catalog.infrastructure.repository.UbicacionRepository;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import com.app.usochicamochabackend.vehicleinspection.application.dto.DocumentoVehiculoResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.KilometrajeValidacionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehicleInspectionReportDTO;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionRequest;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.port.CreateVehiculoInspectionUseCase;
import com.app.usochicamochabackend.vehicleinspection.application.port.GetVehicleInspectionsUseCase;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspDetalleDocumentosEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspDetalleElementosEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspDetalleMecanicoEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspDetalleSaludEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspPreOperativaEntity;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.DocumentacionYElementosRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleDocumentosRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleElementosRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleMecanicoRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspDetalleSaludRepository;
import com.app.usochicamochabackend.vehicleinspection.infrastructure.repository.InspPreOperativaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class VehiculoInspectionService implements CreateVehiculoInspectionUseCase, GetVehicleInspectionsUseCase {

    private final InspPreOperativaRepository inspPreOperativaRepository;
    private final InspDetalleMecanicoRepository detalleMecanicoRepository;
    @Qualifier("vehicleInspDetalleDocumentosRepository")
    private final InspDetalleDocumentosRepository detalleDocumentosRepository;
    private final InspDetalleElementosRepository detalleElementosRepository;
    private final InspDetalleSaludRepository detalleSaludRepository;
    private final DocumentacionYElementosRepository documentacionRepository;
    private final UbicacionRepository ubicacionRepository;
    private final VehicleRepository vehicleRepository;

    /**
     * POST — Guarda la inspección pre-operativa en las 5 tablas de inspección.
     * NO escribe en documentacion_y_elementos (eso lo administra la web/admin).
     */
    @Override
    @Transactional
    public VehiculoInspectionResponse create(VehiculoInspectionRequest req, UserPrincipal inspector) {

        // ── Resolver idVehiculo a partir de la placa ─────────────────────────
        VehicleEntity vehicle = vehicleRepository.findByPlaca(req.placaVehiculo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehículo no encontrado con placa: " + req.placaVehiculo()));

        Integer idVehiculo = vehicle.getIdVehiculo();

        // ── 1: Cabecera — inspeccion_pre_operativa ────────────────────────────
        InspPreOperativaEntity cabecera = InspPreOperativaEntity.builder()
                .fechaRegistro(LocalDateTime.now())
                .idVehiculo(idVehiculo)
                .loginUser(inspector.username()) // username del usuario autenticado
                .kilometrajeReportado(req.kilometrajeReportado() != null ? req.kilometrajeReportado() : 0)
                .aprobadoRuta(req.aprobadoRuta())
                .observacionesFinales(req.observacionesFinales())
                .build();

        InspPreOperativaEntity saved = inspPreOperativaRepository.save(cabecera);
        Long idInspeccion = saved.getIdInspeccion();

        // ── 2: insp_detalle_mecanico ──────────────────────────────────────────
        detalleMecanicoRepository.save(
                InspDetalleMecanicoEntity.builder()
                        .idInspeccion(idInspeccion)
                        .nivelAceite(req.nivelAceite())
                        .nivelRefrigerante(req.nivelRefrigerante())
                        .nivelFrenos(req.nivelFrenos())
                        .estadoLlantas(req.estadoLlantas())
                        .lucesGeneral(req.lucesGeneral())
                        .estadoVisual(req.estadoVisual())
                        .limpiezaGeneral(req.limpiezaGeneral())
                        .build());

        // ── 3: insp_detalle_documentos ────────────────────────────────────────
        // Check visual del inspector (Vigente / Próximo a Vencer / Vencido)
        detalleDocumentosRepository.save(
                InspDetalleDocumentosEntity.builder()
                        .idInspeccion(idInspeccion)
                        .checkSoat(req.checkSoat())
                        .checkTecno(req.checkTecno())
                        .checkLicencia(req.checkLicencia())
                        .checkExtintor(req.checkExtintor())
                        .build());

        // ── 4: insp_detalle_elementos ─────────────────────────────────────────
        detalleElementosRepository.save(
                InspDetalleElementosEntity.builder()
                        .idInspeccion(idInspeccion)
                        .tieneBotiquin(req.tieneBotiquin())
                        .tieneSeñalizacion(req.tieneSeñalizacion())
                        .tieneLineasEmergencia(req.tieneLineasEmergencia()) // → tiene_extintor col.
                        .tieneLlantaRepuesto(booleanToSiNo(req.tieneLlantaRepuesto()))
                        .tieneGatoHidraulico(booleanToSiNo(req.tieneGatoHidraulico()))
                        .build());

        // ── 5: insp_detalle_salud ─────────────────────────────────────────────
        detalleSaludRepository.save(
                InspDetalleSaludEntity.builder()
                        .idInspeccion(idInspeccion)
                        .saludFisica(req.saludFisica())
                        .saludMental(req.saludMental())
                        .sobrio(req.sobrio())
                        .medicamentos(req.medicamentos())
                        .condicionParaConducir(req.condicionParaConducir())
                        .conscienteResponsabilidad(req.conscienteResponsabilidad())
                        .build());

        // ── Actualizar kilometraje del vehículo ───────────────────────────────
        if (req.kilometrajeReportado() != null && req.kilometrajeReportado() > 0) {
            vehicleRepository.updateKilometraje(idVehiculo, req.kilometrajeReportado());
        }

        return new VehiculoInspectionResponse(idInspeccion, "Inspección guardada exitosamente");
    }

    @Override
    public List<VehicleInspectionReportDTO> getInspectionsByType(Integer typeId) {
        List<InspPreOperativaEntity> inspections = inspPreOperativaRepository.findAllByVehicleType(typeId);
        List<VehicleInspectionReportDTO> reportList = new java.util.ArrayList<>();

        for (InspPreOperativaEntity inspection : inspections) {
            Long id = inspection.getIdInspeccion();
            var mecanico = detalleMecanicoRepository.findByIdInspeccion(id).orElse(new InspDetalleMecanicoEntity());
            var docs = detalleDocumentosRepository.findByIdInspeccion(id).orElse(new InspDetalleDocumentosEntity());
            var elementos = detalleElementosRepository.findByIdInspeccion(id).orElse(new InspDetalleElementosEntity());
            var salud = detalleSaludRepository.findByIdInspeccion(id).orElse(new InspDetalleSaludEntity());
            var vehicle = inspection.getVehiculo();

            reportList.add(new VehicleInspectionReportDTO(
                id,
                inspection.getFechaRegistro(),
                vehicle != null ? vehicle.getPlaca() : null,
                (vehicle != null && vehicle.getMarca() != null) ? vehicle.getMarca().getDescripcion() : null,
                (vehicle != null && vehicle.getTipoVehiculo() != null) ? vehicle.getTipoVehiculo().getNombreTipo() : null,
                inspection.getLoginUser(),
                ubicacionRepository.findById(inspection.getIdUbicacion()).map(u -> u.getNombreUbicacion()).orElse("N/A"),
                inspection.getKilometrajeReportado(),
                inspection.getAprobadoRuta(),
                inspection.getObservacionesFinales(),
                
                mecanico.getNivelAceite(),
                mecanico.getNivelRefrigerante(),
                mecanico.getNivelFrenos(),
                mecanico.getEstadoLlantas(),
                mecanico.getLucesGeneral(),
                mecanico.getEstadoVisual(),
                mecanico.getLimpiezaGeneral(),
                
                docs.getCheckSoat(),
                docs.getCheckTecno(),
                docs.getCheckLicencia(),
                docs.getCheckExtintor(),
                
                elementos.getTieneBotiquin(),
                elementos.getTieneSeñalizacion(),
                elementos.getTieneLineasEmergencia(),
                elementos.getTieneLlantaRepuesto(),
                elementos.getTieneGatoHidraulico(),
                
                salud.getSaludFisica(),
                salud.getSaludMental(),
                salud.getSobrio(),
                salud.getMedicamentos(),
                salud.getCondicionParaConducir(),
                salud.getConscienteResponsabilidad()
            ));
        }
        return reportList;
    }

    /**
     * GET — Valida si el kilometraje ingresado por el inspector es menor
     * al kilometraje_actual registrado para el vehículo.
     *
     * @param placa                Placa del vehículo a consultar.
     * @param kilometrajeIngresado Valor que el inspector está por registrar.
     * @return {@link KilometrajeValidacionResponse} con {@code alerta=true} si el
     *         valor ingresado es menor al registrado, {@code false} en caso
     *         contrario.
     */
    public KilometrajeValidacionResponse validarKilometraje(String placa, Integer kilometrajeIngresado) {

        VehicleEntity vehicle = vehicleRepository.findByPlaca(placa)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehículo no encontrado con placa: " + placa));

        Integer kilometrajeActual = vehicle.getKilometrajeActual();

        // Si el vehículo no tiene kilometraje registrado aún, no hay alerta
        if (kilometrajeActual == null || kilometrajeActual == 0) {
            return new KilometrajeValidacionResponse(false, "Sin kilometraje previo registrado.");
        }

        if (kilometrajeIngresado < kilometrajeActual) {
            return new KilometrajeValidacionResponse(
                    true,
                    "El kilometraje ingresado (" + kilometrajeIngresado + " km) es menor al registrado ("
                            + kilometrajeActual + " km).");
        }

        return new KilometrajeValidacionResponse(false, "Kilometraje correcto.");
    }

    /**
     * GET — Lee documentacion_y_elementos (tabla administrada por la web/admin)
     * y retorna las fechas de vencimiento con su estado calculado en tiempo real.
     *
     * Estados: "Vigente" | "Próximo a Vencer" (≤ 30 días) | "Vencido"
     */
    public DocumentoVehiculoResponse getDocumentos(Integer idVehiculo) {

        String fechaSoat = null, estadoSoat = null, urlSoat = null;
        String fechaTecno = null, estadoTecno = null, urlTecno = null;
        String fechaLicencia = null, estadoLicencia = null, urlLicencia = null;
        String fechaExtintor = null, estadoExtintor = null, urlExtintor = null;

        var soat = documentacionRepository.findLatestByVehiculoAndTipo(idVehiculo, "SOAT");
        if (soat.isPresent()) {
            fechaSoat = soat.get().getFechaVencimiento().toString();
            estadoSoat = calcularEstado(soat.get().getFechaVencimiento());
            urlSoat = resolveUrl(soat.get().getImagenUrl());
        }

        var tecno = documentacionRepository.findLatestByVehiculoAndTipo(idVehiculo, "TECNOMECANICA");
        if (tecno.isPresent()) {
            fechaTecno = tecno.get().getFechaVencimiento().toString();
            estadoTecno = calcularEstado(tecno.get().getFechaVencimiento());
            urlTecno = resolveUrl(tecno.get().getImagenUrl());
        }

        var licencia = documentacionRepository.findLatestByVehiculoAndTipo(idVehiculo, "LICENCIA DE CONDUCCION");
        if (licencia.isPresent()) {
            fechaLicencia = licencia.get().getFechaVencimiento().toString();
            estadoLicencia = calcularEstado(licencia.get().getFechaVencimiento());
            urlLicencia = resolveUrl(licencia.get().getImagenUrl());
        }

        var extintor = documentacionRepository.findLatestByVehiculoAndTipo(idVehiculo, "EXTINTOR");
        if (extintor.isPresent()) {
            fechaExtintor = extintor.get().getFechaVencimiento().toString();
            estadoExtintor = calcularEstado(extintor.get().getFechaVencimiento());
            urlExtintor = resolveUrl(extintor.get().getImagenUrl());
        }

        return new DocumentoVehiculoResponse(
                idVehiculo,
                fechaSoat, estadoSoat, urlSoat,
                fechaTecno, estadoTecno, urlTecno,
                fechaLicencia, estadoLicencia, urlLicencia,
                fechaExtintor, estadoExtintor, urlExtintor);
    }

    /**
     * Resuelve una URL de imagen: si es relativa, le concatena el host actual.
     */
    private String resolveUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return null;
        if (rawUrl.toLowerCase().startsWith("http")) return rawUrl;
        
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        String cleanPath = rawUrl.replace("\\", "/");
        if (cleanPath.startsWith("/")) cleanPath = cleanPath.substring(1);
        
        // Lógica Universal: Si ya trae la ruta (ej: uploads/ID/foto.jpg) la usamos,
        // si es solo el nombre, asumimos que está en la carpeta de documentos.
        if (cleanPath.toLowerCase().startsWith("uploads/")) {
            return baseUrl + "/" + cleanPath;
        } else {
            return baseUrl + "/uploads/documents/" + cleanPath;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Calcula el estado del documento comparando el MES de vencimiento
     * con el MES actual.
     */
    private String calcularEstado(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) return null;
        
        YearMonth mesVenc = YearMonth.from(fechaVencimiento);
        YearMonth mesHoy  = YearMonth.now();

        // 1. Si el mes ya pasó -> Vencido
        if (mesVenc.isBefore(mesHoy)) {
            return "Vencido";
        }

        // 2. Si el mes de vencimiento es HOY -> Vigente (para no bloquear hoy)
        if (mesVenc.equals(mesHoy)) {
            return "Vigente";
        }

        // 3. Si el mes de vencimiento es el SIGUIENTE -> Próximo a Vencer
        if (mesVenc.equals(mesHoy.plusMonths(1))) {
            return "Próximo a Vencer";
        }

        // 4. Si es después del mes siguiente
        return "Vigente";
    }

    private String booleanToSiNo(Boolean value) {
        if (value == null)
            return null;
        return value ? "Si" : "No";
    }
}