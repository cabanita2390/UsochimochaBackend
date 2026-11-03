package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** "Elementos más intervenidos" (equivale a la hoja DASH_CRITICIDAD del Sheet eléctrico). */
public record ElementoIntervenidoResponse(
        @Schema(example = "4") Long elementoId,
        @Schema(example = "Breaker") String nombre,
        @Schema(description = "Visitas del periodo que lo marcaron", example = "9") Integer intervenciones,
        @Schema(description = "Estaciones distintas donde se intervino", example = "5") Integer estaciones
) {
}
