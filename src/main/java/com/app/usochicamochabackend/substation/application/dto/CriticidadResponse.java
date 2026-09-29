package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** "Actividades más críticas" del Detalle por estación: intervenciones acumuladas (histórico). */
public record CriticidadResponse(
        @Schema(example = "11") Long actividadId,
        @Schema(example = "Pintura muros estaciones (segun estado)") String actividadNombre,
        @Schema(example = "Pintura muros") String nombreCorto,
        @Schema(description = "Ejecuciones de esta actividad en la estación, todos los años", example = "4") Integer intervenciones
) {
}
