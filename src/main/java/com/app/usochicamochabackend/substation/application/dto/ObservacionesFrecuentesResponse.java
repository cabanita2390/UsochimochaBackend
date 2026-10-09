package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Observaciones más usadas de un tipo de actividad, para las sugerencias del formulario del móvil.
 * Salen de los registros reales (no de una lista fija): las más repetidas primero.
 */
public record ObservacionesFrecuentesResponse(
        @Schema(description = "Tipo de actividad", example = "MANTENIMIENTO") String tipoActividad,
        @Schema(description = "Observaciones más usadas, de más a menos frecuente") List<String> textos
) {
}
