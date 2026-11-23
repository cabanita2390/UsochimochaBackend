package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Un elemento marcado en la visita, dentro de su conjunto. */
public record ElementoMarcadoRequest(
        @Schema(description = "Conjunto donde se trabajó (debe ir también en conjuntoIds)", example = "7") Long conjuntoId,
        @Schema(description = "Elemento del conjunto", example = "4") Long elementoId,
        @Schema(description = "Texto del elemento que lo pide (ej. \"Estado físico\"); null si no aplica. Máx. 500.")
        String detalle
) {
}
