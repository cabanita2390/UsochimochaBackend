package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoElementoEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/** Un elemento dentro de un conjunto, en el orden en que se muestra. */
public record ElementoDeConjuntoResponse(
        @Schema(description = "ID del elemento (el nombre que se reutiliza entre conjuntos)", example = "4") Long id,
        @Schema(example = "Estado físico") String nombre,
        @Schema(description = "Al marcarlo se escribe cómo se encuentra") Boolean pideDetalle,
        @Schema(description = "Lo agregó un operario con \"Otro\": falta revisarlo") Boolean provisional
) {
    public static ElementoDeConjuntoResponse fromEntity(ConjuntoElementoEntity ce) {
        return new ElementoDeConjuntoResponse(ce.getElemento().getId(), ce.getElemento().getNombre(),
                ce.getPideDetalle(), ce.getProvisional());
    }
}
