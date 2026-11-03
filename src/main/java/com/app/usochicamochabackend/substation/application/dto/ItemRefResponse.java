package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Conjunto o elemento marcado en una visita: lo justo para mostrarlo sin consultar el catálogo. */
public record ItemRefResponse(
        @Schema(example = "3") Long id,
        @Schema(example = "Breaker") String nombre
) {
}
