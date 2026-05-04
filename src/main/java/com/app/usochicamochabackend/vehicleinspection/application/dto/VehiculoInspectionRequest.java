package com.app.usochicamochabackend.vehicleinspection.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO de la inspección pre-operativa de vehículo enviada desde el móvil.
 * <p>
 * Los checks de documentación van a {@code insp_detalle_documentos}.
 * Si el cliente envía fechas / URLs opcionales, se fusionan en
 * {@code documentacion_y_elementos} (misma lógica que el alta desde web/admin).
 * Campos desconocidos (p. ej. marca informativa) se ignoran en JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VehiculoInspectionRequest(

        // ── inspeccion_pre_operativa ──────────────────────────────────────────
        String placaVehiculo, // el Android envía la placa, no el id
        Integer kilometrajeReportado,
        Boolean aprobadoRuta,
        String observacionesFinales,

        // ── insp_detalle_mecanico ─────────────────────────────────────────────
        String nivelAceite,
        String nivelRefrigerante,
        String nivelFrenos,
        String estadoLlantas,
        String lucesGeneral,
        String estadoVisual,
        String limpiezaGeneral,

        // ── insp_detalle_documentos ───────────────────────────────────────────
        // Check visual del inspector: "Vigente" | "Próximo a Vencer" | "Vencido"
        String checkSoat,
        String checkTecno,
        String checkLicencia,
        String checkExtintor,

        // ── documentacion_y_elementos (opcional, desde móvil) ─────────────────
        /** Fecha vencimiento SOAT (ISO yyyy-MM-dd o prefijo de ISO-8601). */
        String fechaVencSoat,
        /** Fecha vencimiento tecnomecánica. */
        String fechaVencTecno,
        /** Fecha vencimiento licencia. */
        String fechaVencLicencia,
        /** Vigencia extintor en formato {@code yyyy-MM}. */
        String vigenciaExtintor,
        String urlImagenSoat,
        String urlImagenTecno,
        String urlImagenLicencia,
        String urlImagenExtintor,

        // ── insp_detalle_elementos ────────────────────────────────────────────
        Boolean tieneBotiquin,
        Boolean tieneSeñalizacion,
        Boolean tieneLineasEmergencia, // → columna tiene_extintor en BD
        Boolean tieneLlantaRepuesto,
        Boolean tieneGatoHidraulico,

        // ── insp_detalle_salud ────────────────────────────────────────────────
        Boolean saludFisica,
        Boolean saludMental,
        Boolean sobrio,
        Boolean medicamentos,
        Boolean conscienteResponsabilidad,
        Boolean condicionParaConducir,
        Integer idUbicacion) {
}
