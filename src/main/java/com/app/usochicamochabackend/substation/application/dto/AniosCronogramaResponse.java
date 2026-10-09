package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Años que se pueden consultar y programar (para los selectores de año de la web). */
public record AniosCronogramaResponse(
        @Schema(description = "Año actual del servidor (hora de Colombia)", example = "2026") Integer anioActual,
        @Schema(description = "Años que se pueden programar: el actual y el siguiente", example = "[2026, 2027]") List<Integer> programables,
        @Schema(description = "Años con citas o registros, del más reciente al más antiguo", example = "[2026, 2025]") List<Integer> conDatos
) {
}
