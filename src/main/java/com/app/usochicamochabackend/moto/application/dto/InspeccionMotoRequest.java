package com.app.usochicamochabackend.moto.application.dto;

/**
 * Request body for saving a motorcycle pre-operational inspection.
 * responsableInspeccion is NOT included here — it's taken from the JWT token
 * server-side.
 */
public record InspeccionMotoRequest(
                Integer idVehiculo,
                Integer idUbicacion,
                Integer kilometrajeReportado,
                String estadoGeneral,
                String observacionesFinales,

                // Documentación — fechas formato "YYYY-MM"
                String vigenciaSoat,
                String vigenciaRevision,
                String vigenciaLicencia,

                // Estado de documentos: "Vigente" | "Próximo a vencer" | "Vencido"
                String estadoSoat,
                String estadoRevision,
                String estadoLicencia,

                // Imágenes como URLs (ya subidas al servidor)
                String imagenSoat,
                String imagenRevision,
                String imagenLicencia) {
}
