package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cronograma anual para la grilla web (estación × mes). Trae BORRADOR y PUBLICADA; nunca
 * RETIRADA ni citas de estaciones inactivas. Los nombres de estación y actividad no se
 * repiten: la web ya tiene los catálogos (/estaciones, /actividades).
 */
public record CronogramaResponse(
        @Schema(description = "Año consultado", example = "2026") Integer anio,
        @Schema(description = "Año actual (servidor, hora de Colombia)", example = "2026") Integer anioActual,
        @Schema(description = "Mes actual 1-12 (servidor, hora de Colombia). Los meses anteriores del año actual están cerrados", example = "9") Integer mesActual,
        @Schema(description = "Última publicación vigente del año (no revertida); null si el año nunca se publicó") UltimaPublicacion ultimaPublicacion,
        @Schema(description = "Cambios pendientes de publicar (de todas las disciplinas)") Borrador borrador,
        @Schema(description = "true si hay una publicación que se puede deshacer y el borrador está vacío") Boolean puedeDeshacer,
        @Schema(description = "Citas vigentes del año (filtradas por disciplina si se pidió)") List<Cita> citas
) {

    public record UltimaPublicacion(
            @Schema(example = "4") Long id,
            @Schema(example = "2026-09-20T16:05:00") LocalDateTime publicadoEn,
            @Schema(description = "Nombre de quien publicó; \"Carga inicial\" para la publicación inicial", example = "Laura Méndez") String usuario,
            @Schema(example = "12") Integer altas,
            @Schema(example = "3") Integer bajas,
            @Schema(description = "true para la carga inicial (no se puede deshacer)") Boolean inicial
    ) {
    }

    public record Borrador(
            @Schema(description = "Citas nuevas en borrador", example = "1") Integer altas,
            @Schema(description = "Citas publicadas marcadas para quitar", example = "0") Integer bajas
    ) {
    }

    public record Cita(
            @Schema(example = "812") Long id,
            @Schema(description = "Mes 1-12", example = "11") Integer mes,
            @Schema(example = "1") Long estacionId,
            @Schema(example = "3") Long actividadId,
            @Schema(example = "CIVIL") String disciplina,
            @Schema(description = "BORRADOR o PUBLICADA", example = "BORRADOR") String estado,
            @Schema(description = "Publicada que se quitará al publicar (se muestra tachada)") Boolean pendienteRetiro,
            @Schema(description = "Tiene al menos una ejecución registrada") Boolean tieneEjecucion,
            @Schema(description = "Fecha de la primera ejecución; null si no tiene") LocalDate fechaEjecucion
    ) {
    }
}
