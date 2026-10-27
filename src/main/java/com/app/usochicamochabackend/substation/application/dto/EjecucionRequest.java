package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EjecucionRequest(
        @Schema(description = "Fecha de la visita", example = "2026-09-07") LocalDate fecha,
        @Schema(description = "Mes de ejecución (1-12), editable si el registro es tardío", example = "9") Integer mesEjecucion,
        @Schema(description = "Semana de ejecución (1-4)", example = "2") Integer semanaEjecucion,
        @Schema(description = "ID de la estación visitada", example = "1") Long estacionId,
        @Schema(description = "Disciplina", example = "CIVIL") String disciplina,
        @Schema(description = "Tipo de mantenimiento: PREVENTIVO, CORRECTIVO o PREDICTIVO. NO_PROGRAMADO es obsoleto (si el trabajo estaba en el cronograma lo dice esProgramada); se sigue aceptando solo por registros viejos y colas offline de versiones anteriores del móvil.", example = "PREVENTIVO") String tipoMantenimiento,
        @Schema(description = "Tipo de actividad: INSPECCION o MANTENIMIENTO (OTRO fuera de Civil). NO_PROGRAMADO es obsoleto (lo dice esProgramada); se sigue aceptando solo por compatibilidad.", example = "MANTENIMIENTO") String tipoActividad,
        @Schema(description = "ID de la actividad del catálogo; null si no vino de una cita del cronograma") Long actividadId,
        @Schema(description = "ID de la cita de programación; null si no vino de una cita del cronograma") Long programacionId,
        @Schema(description = "Motivo cuando actividadId es null: NO_PROGRAMADO u OTRO") String motivoNoCatalogado,
        @Schema(description = "Resultado de la visita", example = "CONFORME") String resultado,
        @Schema(description = "Observaciones de la actividad realizada") String observaciones,
        @Schema(description = "Descripción libre; requerida cuando actividadId es null") String descripcionLibre,
        @Schema(description = "UUID generado por el cliente para idempotencia offline") UUID uuidCliente,
        @Schema(description = "Orden de trabajo que esta visita ejecuta; null en citas e imprevistos") Long ordenId,
        @Schema(description = "Eléctrico: IDs de los conjuntos intervenidos (opcional). Los que no son de la disciplina se ignoran.")
        List<Long> conjuntoIds,
        @Schema(description = "Eléctrico: IDs de los elementos inspeccionados o intervenidos (opcional)")
        List<Long> elementoIds,
        @Schema(description = "Eléctrico: texto de \"Otros\" en conjuntos (opcional, máx. 200)", example = "Tablero de bombas auxiliares")
        String conjuntoOtro,
        @Schema(description = "Eléctrico: texto de \"Otros\" en elementos (opcional, máx. 200)")
        String elementoOtro
) {
    /** Sin conjuntos ni elementos (Civil; versiones del móvil anteriores a Eléctrico). */
    public EjecucionRequest(LocalDate fecha, Integer mesEjecucion, Integer semanaEjecucion, Long estacionId,
            String disciplina, String tipoMantenimiento, String tipoActividad, Long actividadId, Long programacionId,
            String motivoNoCatalogado, String resultado, String observaciones, String descripcionLibre,
            UUID uuidCliente, Long ordenId) {
        this(fecha, mesEjecucion, semanaEjecucion, estacionId, disciplina, tipoMantenimiento, tipoActividad,
                actividadId, programacionId, motivoNoCatalogado, resultado, observaciones, descripcionLibre,
                uuidCliente, ordenId, null, null, null, null);
    }

    /** Sin orden de trabajo (citas del cronograma e imprevistos; versiones del móvil anteriores a las OT). */
    public EjecucionRequest(LocalDate fecha, Integer mesEjecucion, Integer semanaEjecucion, Long estacionId,
            String disciplina, String tipoMantenimiento, String tipoActividad, Long actividadId, Long programacionId,
            String motivoNoCatalogado, String resultado, String observaciones, String descripcionLibre,
            UUID uuidCliente) {
        this(fecha, mesEjecucion, semanaEjecucion, estacionId, disciplina, tipoMantenimiento, tipoActividad,
                actividadId, programacionId, motivoNoCatalogado, resultado, observaciones, descripcionLibre,
                uuidCliente, null);
    }
}
