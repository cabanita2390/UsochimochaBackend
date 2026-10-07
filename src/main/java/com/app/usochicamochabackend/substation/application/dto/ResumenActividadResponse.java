package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Resumen por actividad: todo del año pedido y solo del cronograma publicado.
 * % de cumplimiento con la misma fórmula del Dashboard (ejecutadasVencidas / vencidas).
 */
public record ResumenActividadResponse(
        @Schema(description = "ID de la actividad", example = "1") Long actividadId,
        @Schema(description = "Nombre de la actividad") String actividadNombre,
        @Schema(description = "Código de disciplina", example = "CIVIL") String disciplina,
        @Schema(description = "Citas publicadas del año para esta actividad, todas las estaciones") Integer programadoAnual,
        @Schema(description = "Ejecuciones del año registradas para esta actividad") Integer ejecutadoAnual,
        @Schema(description = "Ejecuciones del año sin cita asociada") Integer ejecutadoNoProgramado,
        @Schema(description = "Ejecuciones del año de tipo MANTENIMIENTO") Integer mantenimiento,
        @Schema(description = "Ejecuciones del año de tipo INSPECCION") Integer inspeccion,
        @Schema(description = "Total de ejecuciones del año") Integer ejecutadoTotal,
        @Schema(description = "Año consultado", example = "2026") Integer anio,
        @Schema(description = "Citas publicadas del año que ya entran al %: las de meses cerrados más las ya ejecutadas de meses abiertos") Integer vencidas,
        @Schema(description = "De las que entran al %, las que tienen ejecución") Integer ejecutadasVencidas,
        @Schema(description = "ejecutadasVencidas / vencidas × 100, 0-100; null si no hay citas vencidas") BigDecimal porcentajeCumplimiento
) {
}
