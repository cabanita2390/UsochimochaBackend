package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record CrearOrdenRequest(
        @Schema(description = "Ejecución con el hallazgo que se corrige", example = "1234") Long ejecucionOrigenId,
        @Schema(description = "ALTA o MEDIA", example = "ALTA") String prioridad,
        @Schema(description = "Fecha límite (hoy o después)", example = "2026-10-25") LocalDate fechaLimite,
        @Schema(description = "Operario (usuario OPERARIO activo) que la ejecuta", example = "12") Long asignadoAId,
        @Schema(description = "Actividad del catálogo con la que se registra la corrección (misma disciplina del hallazgo)", example = "5") Long actividadId,
        @Schema(description = "Qué hay que hacer", example = "Sellar la fisura del muro norte con mortero epóxico.") String instrucciones
) {
}
