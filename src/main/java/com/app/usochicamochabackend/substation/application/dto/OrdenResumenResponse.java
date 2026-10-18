package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenTrabajoEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Fila de la lista de órdenes de trabajo. */
public record OrdenResumenResponse(
        Long id,
        @Schema(example = "OT-EB-00012") String consecutivo,
        @Schema(allowableValues = {"ASIGNADA", "POR_VERIFICAR", "CERRADA", "ANULADA"}) String estado,
        @Schema(allowableValues = {"ALTA", "MEDIA"}) String prioridad,
        LocalDate fechaLimite,
        @Schema(description = "Sigue en curso y la fecha límite ya pasó") Boolean vencida,
        @Schema(description = "Días desde que se creó hasta hoy, o hasta que se cerró o anuló") Long diasAbierta,
        Long estacionId,
        String estacionNombre,
        String disciplina,
        Long actividadId,
        String actividadNombre,
        @Schema(description = "null si es para cualquier operario y nadie la ha tomado") Long asignadoAId,
        String asignadoANombre,
        @Schema(description = "true si es para cualquier operario y nadie la ha tomado todavía") Boolean paraCualquiera,
        String instrucciones,
        String creadoPor,
        LocalDateTime creadoEn,
        @Schema(description = "Ejecución en que se encontró el hallazgo") Long hallazgoEjecucionId,
        LocalDate hallazgoFecha,
        String hallazgoResultado,
        String hallazgoObservaciones
) {
    public static OrdenResumenResponse fromEntity(OrdenTrabajoEntity o, LocalDate hoy) {
        EjecucionEntity h = o.getEjecucionOrigen();
        LocalDate fin = o.enCurso() ? hoy : o.getActualizadoEn().toLocalDate();
        return new OrdenResumenResponse(
                o.getId(), o.getConsecutivo(), o.getEstado(), o.getPrioridad(), o.getFechaLimite(),
                o.enCurso() && o.getFechaLimite().isBefore(hoy),
                Math.max(0, ChronoUnit.DAYS.between(o.getCreadoEn().toLocalDate(), fin)),
                o.getEstacion().getId(), o.getEstacion().getNombre(), o.getDisciplina().getCodigo(),
                o.getActividad().getId(), o.getActividad().getNombre(),
                o.getAsignadoA() != null ? o.getAsignadoA().getId() : null, Nombres.de(o.getAsignadoA()),
                o.getAsignadoA() == null, o.getInstrucciones(),
                Nombres.de(o.getCreadoPor()), o.getCreadoEn(),
                h.getId(), h.getFecha(), h.getResultado(), h.getObservaciones());
    }
}
