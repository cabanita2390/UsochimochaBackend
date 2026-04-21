package com.app.usochicamochabackend.vehicleinspection.application.dto;

/**
 * Respuesta del GET /api/v1/vehicle-inspection/documentos/{idVehiculo}
 * Retorna la fecha de vencimiento más reciente de cada tipo de documento
 * y su estado calculado automáticamente (Vigente / Próximo a Vencer / Vencido).
 */
public record DocumentoVehiculoResponse(
        Integer idVehiculo,

        // SOAT
        String fechaVencSoat, // "YYYY-MM-DD"
        String estadoSoat, // "Vigente" | "Próximo a Vencer" | "Vencido"
        String urlImagenSoat,

        // Tecnomecánica
        String fechaVencTecno,
        String estadoTecno,
        String urlImagenTecno,

        // Licencia de conducción
        String fechaVencLicencia,
        String estadoLicencia,
        String urlImagenLicencia,

        // Extintor
        String fechaVencExtintor,
        String estadoExtintor,
        String urlImagenExtintor) {
}
