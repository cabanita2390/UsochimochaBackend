package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ResolverHallazgoRequest(
        @Schema(description = "Qué se hizo para resolverlo", example = "Se selló la fisura con mortero epóxico.")
        @NotBlank String observacionesCierre,

        @Schema(description = "Opcional: ejecución posterior de la misma estación en la que se resolvió", example = "1234")
        Long resueltoEnEjecucionId,

        @Schema(description = "Opcional: true si se resolvió en la misma visita. Excluye resueltoEnEjecucionId", example = "false")
        Boolean resueltoMismaVisita
) {
}
