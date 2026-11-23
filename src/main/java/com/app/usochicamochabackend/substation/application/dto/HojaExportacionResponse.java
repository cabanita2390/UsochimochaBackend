package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Una hoja que se puede descargar a Excel desde Estaciones, con sus columnas. */
public record HojaExportacionResponse(
        @Schema(example = "EJECUCIONES") String clave,
        @Schema(example = "Ejecuciones") String titulo,
        @Schema(description = "Qué filtros usa la hoja: disciplina, anio, mes, estacionId") List<String> filtros,
        List<Columna> columnas
) {
    public record Columna(
            @Schema(example = "fecha") String clave,
            @Schema(example = "Fecha") String titulo,
            @Schema(description = "Sale marcada por defecto") boolean porDefecto
    ) {
    }
}
