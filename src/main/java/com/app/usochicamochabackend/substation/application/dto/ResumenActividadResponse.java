package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Resumen por actividad: cada actividad del cronograma en todas las estaciones, del año pedido
 * y solo de lo publicado, más el corte del mes en curso (mismos campos que el Dashboard de
 * estaciones, pero agrupado por actividad en vez de por estación).
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
        @Schema(description = "ejecutadasVencidas / vencidas × 100, 0-100; null si no hay citas vencidas") BigDecimal porcentajeCumplimiento,
        @Schema(description = "Citas del año con al menos una ejecución registrada (avance del año = cumple / programadoAnual)") Integer cumple,
        @Schema(description = "Estaciones distintas con citas publicadas de esta actividad en el año") Integer estaciones,
        @Schema(description = "Mes en curso (1-12); null si se consulta otro año") Integer mes,
        @Schema(description = "Citas publicadas del mes en curso; null si se consulta otro año") Integer programadoMes,
        @Schema(description = "De las citas del mes en curso, las que ya tienen ejecución") Integer cumpleMes,
        @Schema(description = "Ejecuciones de la actividad en el mes en curso (programadas y no programadas)") Integer ejecutadoTotalMes,
        @Schema(description = "Ejecuciones de la actividad en el mes en curso sin cita asociada (imprevistos)") Integer ejecutadoNoProgramadoMes,
        @Schema(description = "Porción del mes en curso ya transcurrida, 0-100 (para comparar el avance del mes)") BigDecimal porcentajeMesTranscurrido
) {
}
