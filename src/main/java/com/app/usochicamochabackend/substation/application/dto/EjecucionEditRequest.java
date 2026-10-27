package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * Corrección de datos de una ejecución ya registrada. La estación, la disciplina,
 * la cita de programación y el uuidCliente NO son editables aquí: editar es
 * corregir lo que se reportó de la visita, no reasignarla a otra estación/cita.
 */
public record EjecucionEditRequest(
        @Schema(description = "Fecha de la visita", example = "2026-09-07") LocalDate fecha,
        @Schema(description = "Mes de ejecución (1-12)", example = "9") Integer mesEjecucion,
        @Schema(description = "Semana de ejecución (1-4)", example = "2") Integer semanaEjecucion,
        @Schema(description = "Tipo de mantenimiento: PREVENTIVO, CORRECTIVO o PREDICTIVO. NO_PROGRAMADO es obsoleto (si el trabajo estaba en el cronograma lo dice esProgramada); se sigue aceptando solo por registros viejos y colas offline de versiones anteriores del móvil.", example = "PREVENTIVO") String tipoMantenimiento,
        @Schema(description = "Tipo de actividad: INSPECCION o MANTENIMIENTO (OTRO fuera de Civil). NO_PROGRAMADO es obsoleto (lo dice esProgramada); se sigue aceptando solo por compatibilidad.", example = "MANTENIMIENTO") String tipoActividad,
        @Schema(description = "ID de la actividad del catálogo; null si no vino de una cita del cronograma") Long actividadId,
        @Schema(description = "Motivo cuando actividadId es null") String motivoNoCatalogado,
        @Schema(description = "Resultado de la visita", example = "CONFORME") String resultado,
        @Schema(description = "Observaciones de la actividad realizada") String observaciones,
        @Schema(description = "Descripción libre; requerida cuando actividadId es null") String descripcionLibre,
        @Schema(description = "Motivo de la edición, mínimo 15 caracteres. Queda en el historial del registro.")
        String motivoEdicion,
        @Schema(description = "Eléctrico: conjuntos intervenidos. null = no se cambian; lista vacía = se quitan todos.")
        List<Long> conjuntoIds,
        @Schema(description = "Eléctrico: elementos intervenidos. null = no se cambian; lista vacía = se quitan todos.")
        List<Long> elementoIds,
        @Schema(description = "Eléctrico: \"Otros\" en conjuntos. null = no se cambia; vacío = se borra.")
        String conjuntoOtro,
        @Schema(description = "Eléctrico: \"Otros\" en elementos. null = no se cambia; vacío = se borra.")
        String elementoOtro
) {
    /**
     * Sin tocar conjuntos ni elementos: Civil y las versiones del móvil anteriores a Eléctrico, cuyo
     * "Editar registro" no los envía (null) y no debe borrarlos.
     */
    public EjecucionEditRequest(LocalDate fecha, Integer mesEjecucion, Integer semanaEjecucion,
            String tipoMantenimiento, String tipoActividad, Long actividadId, String motivoNoCatalogado,
            String resultado, String observaciones, String descripcionLibre, String motivoEdicion) {
        this(fecha, mesEjecucion, semanaEjecucion, tipoMantenimiento, tipoActividad, actividadId,
                motivoNoCatalogado, resultado, observaciones, descripcionLibre, motivoEdicion,
                null, null, null, null);
    }
}
