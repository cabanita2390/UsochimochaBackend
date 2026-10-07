package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Panel de celda (1 estación) o Asignación masiva (varias). Todo queda en BORRADOR. */
public record AsignarCitasRequest(
        @Schema(description = "Año del cronograma", example = "2026") @NotNull Integer anio,
        @Schema(description = "Actividad activa", example = "3") @NotNull Long actividadId,
        @Schema(description = "Estaciones", example = "[1, 4, 7]") @NotEmpty List<Long> estacionIds,
        @Schema(description = "Meses 1-12", example = "[10, 11, 12]") @NotEmpty List<Integer> meses
) {
}
