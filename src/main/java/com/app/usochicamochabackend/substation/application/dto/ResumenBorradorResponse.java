package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Modal "Publicar cronograma a móvil": qué cambia, por estación y en orden de mes. */
public record ResumenBorradorResponse(
        @Schema(description = "Citas nuevas (+)", example = "5") Integer altas,
        @Schema(description = "Citas que se quitarán (−)", example = "2") Integer bajas,
        @Schema(example = "3") Integer estacionesAfectadas,
        List<Estacion> porEstacion
) {

    public record Estacion(
            @Schema(example = "1") Long estacionId,
            @Schema(example = "Ayalas") String estacionNombre,
            List<Cambio> cambios
    ) {
    }

    public record Cambio(
            @Schema(example = "812") Long citaId,
            @Schema(description = "Mes 1-12", example = "11") Integer mes,
            @Schema(example = "Pintura muros estaciones (segun estado)") String actividadNombre,
            @Schema(description = "ALTA o BAJA", example = "ALTA") String tipo
    ) {
    }
}
