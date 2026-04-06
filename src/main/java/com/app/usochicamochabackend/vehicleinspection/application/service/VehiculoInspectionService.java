package com.app.usochicamochabackend.vehicleinspection.application.service;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import com.app.usochicamochabackend.vehicleinspection.application.dto.DocumentoVehiculoResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.KilometrajeValidacionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionRequest;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.port.CreateVehiculoInspectionUseCase;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class VehiculoInspectionService implements CreateVehiculoInspectionUseCase {

    private final InspPreOperativaRepository inspPreOperativaRepository;
    private final InspDetalleMecanicoRepository detalleMecanicoRepository;
    @Qualifier("vehicleInspDetalleDocumentosRepository")
    private final InspDetalleDocumentosRepository detalleDocumentosRepository;
    private final InspDetalleElementosRepository detalleElementosRepository;
    private final InspDetalleSaludRepository detalleSaludRepository;
    private final DocumentacionYElementosRepository documentacionRepository;
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
            urlSoat = soat.get().getImagenUrl();
        }

        var tecno = documentacionRepository.findLatestByVehiculoAndTipo(idVehiculo, "TECNOMECANICA");
        if (tecno.isPresent()) {
            fechaTecno = tecno.get().getFechaVencimiento().toString();
            estadoTecno = calcularEstado(tecno.get().getFechaVencimiento());
            urlTecno = tecno.get().getImagenUrl();
        }

        var licencia = documentacionRepository.findLatestByVehiculoAndTipo(idVehiculo, "LICENCIA DE CONDUCCION");
        if (licencia.isPresent()) {
            fechaLicencia = licencia.get().getFechaVencimiento().toString();
            estadoLicencia = calcularEstado(licencia.get().getFechaVencimiento());
            urlLicencia = licencia.get().getImagenUrl();
        }

        var extintor = documentacionRepository.findLatestByVehiculoAndTipo(idVehiculo, "EXTINTOR");
        if (extintor.isPresent()) {
            fechaExtintor = extintor.get().getFechaVencimiento().toString();
            estadoExtintor = calcularEstado(extintor.get().getFechaVencimiento());
            urlExtintor = extintor.get().getImagenUrl();
        }

        return new DocumentoVehiculoResponse(
                idVehiculo,
                fechaSoat, estadoSoat, urlSoat,
                fechaTecno, estadoTecno, urlTecno,
                fechaLicencia, estadoLicencia, urlLicencia,
                fechaExtintor, estadoExtintor, urlExtintor);
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