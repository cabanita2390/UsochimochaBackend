package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** "Copiar {año} como borrador": copia las citas publicadas del origen al destino, como BORRADOR. */
public record CopiarAnioRequest(
        @Schema(example = "2026") @NotNull Integer anioOrigen,
        @Schema(example = "2027") @NotNull Integer anioDestino
) {
}
