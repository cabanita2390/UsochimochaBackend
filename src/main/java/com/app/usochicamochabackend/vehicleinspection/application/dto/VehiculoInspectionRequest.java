package com.app.usochicamochabackend.vehicleinspection.application.dto;

/**
 * DTO de la inspección pre-operativa de vehículo enviada desde el móvil.
 *
 * IMPORTANTE: Las fechas de vencimiento y fotos de documentos NO van aquí.
 * Esos datos los administra la web/admin en la tabla documentacion_y_elementos.
 * El móvil solo envía los checks visuales del inspector
 * (Vigente/Próximo/Vencido).
 */
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
        Boolean condicionParaConducir) {
}
