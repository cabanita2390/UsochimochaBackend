package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/** Solo mientras la orden está ASIGNADA: reasignar, cambiar plazo, prioridad, actividad o instrucciones. */
public record EditarOrdenRequest(
        @Schema(example = "MEDIA") String prioridad,
        @Schema(example = "2026-11-05") LocalDate fechaLimite,
        @Schema(example = "12") Long asignadoAId,
        @Schema(example = "5") Long actividadId,
        String instrucciones
) {
}
