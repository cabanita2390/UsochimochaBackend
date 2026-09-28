package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(
        @Schema(description = "false = desactivar, true = reactivar", example = "false")
        @NotNull Boolean activa
) {
}
