package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Resultado de publicar o deshacer. noAplicadas: citas que no se movieron para no perder una ejecución. */
public record PublicacionResultado(
        @Schema(description = "Publicación creada (publicar) o revertida (deshacer)", example = "5") Long publicacionId,
        @Schema(example = "5") Integer altas,
        @Schema(example = "1") Integer bajas,
        List<NoAplicada> noAplicadas
) {

    public record NoAplicada(
            @Schema(example = "90") Long citaId,
            @Schema(example = "Tiene ejecución registrada") String motivo
    ) {
    }
}
