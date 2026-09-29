package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Dashboard de estaciones: todo del año y la disciplina pedidos, y solo del cronograma
 * publicado. % de cumplimiento = ejecutadasVencidas / vencidas (las citas del mes en curso y
 * de meses futuros no cuentan como incumplidas).
 */
public record IndicadorEstacionResponse(
        @Schema(description = "ID de la estación", example = "1") Long estacionId,
        @Schema(description = "Nombre de la estación", example = "Duitama") String estacionNombre,
        @Schema(description = "Tipo de estación", example = "BOMBEO") String estacionTipo,
        @Schema(description = "Citas publicadas del año") Integer programado,
        @Schema(description = "Citas del año con al menos una ejecución registrada") Integer cumple,
        @Schema(description = "Citas del año sin ejecución (incluye las de meses que aún no terminan)") Integer noCumple,
        @Schema(description = "ejecutadasVencidas / vencidas × 100, 0-100; null si no hay citas vencidas") BigDecimal porcentajeCumplimiento,
        @Schema(description = "Ejecuciones del año que correspondían a una cita del cronograma") Integer ejecutadoProgramado,
        @Schema(description = "Ejecuciones del año sin cita asociada (no previstas u otras)") Integer ejecutadoNoProgramado,
        @Schema(description = "Ejecuciones del año de tipo MANTENIMIENTO") Integer ejecutadoMantenimiento,
        @Schema(description = "Ejecuciones del año de tipo INSPECCION") Integer ejecutadoInspeccion,
        @Schema(description = "Total de ejecuciones del año en la estación") Integer ejecutadoTotal,
        @Schema(description = "Año consultado", example = "2026") Integer anio,
        @Schema(description = "Citas publicadas del año con mes cerrado") Integer vencidas,
        @Schema(description = "De las vencidas, las que tienen ejecución") Integer ejecutadasVencidas
) {
}
