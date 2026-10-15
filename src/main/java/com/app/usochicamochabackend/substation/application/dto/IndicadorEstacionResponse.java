package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Dashboard de estaciones: todo del año y la disciplina pedidos, y solo del cronograma
 * publicado, más el corte del mes en curso. % de cumplimiento = ejecutadasVencidas / vencidas (las citas pendientes del mes en
 * curso y de meses futuros no cuentan como incumplidas; las ya ejecutadas sí suman).
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
        @Schema(description = "Citas publicadas del año que ya entran al %: las de meses cerrados más las ya ejecutadas de meses abiertos") Integer vencidas,
        @Schema(description = "De las que entran al %, las que tienen ejecución") Integer ejecutadasVencidas,
        @Schema(description = "Ejecuciones del año con resultado distinto de CONFORME") Integer conHallazgos,
        @Schema(description = "Hallazgos ABIERTO o EN_PROCESO de la estación (todos los años)") Integer hallazgosAbiertos,
        @Schema(description = "false si la estación está desactivada (solo sale si tuvo citas o registros ese año)") Boolean activa,
        @Schema(description = "Mes en curso (1-12); null si se consulta otro año") Integer mes,
        @Schema(description = "Citas publicadas del mes en curso; null si se consulta otro año") Integer programadoMes,
        @Schema(description = "De las citas del mes en curso, las que ya tienen ejecución") Integer cumpleMes,
        @Schema(description = "Ejecuciones registradas en el mes en curso (programadas y no programadas)") Integer ejecutadoTotalMes,
        @Schema(description = "Ejecuciones del mes en curso sin cita asociada (imprevistos)") Integer ejecutadoNoProgramadoMes,
        @Schema(description = "Porción del mes en curso ya transcurrida, 0-100 (para comparar el avance del mes)") BigDecimal porcentajeMesTranscurrido
) {
}
