package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionElementoEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/** Elemento marcado en una visita, con su conjunto. id y nombre son los del elemento. */
public record ElementoMarcadoResponse(
        @Schema(example = "3") Long id,
        @Schema(example = "Breaker") String nombre,
        @Schema(description = "Conjunto donde se trabajó; null en visitas anteriores al ajuste (\"sin conjunto\")")
        Long conjuntoId,
        @Schema(example = "Gabinetes de control") String conjuntoNombre,
        @Schema(description = "Texto del elemento que lo pide (\"Estado físico\"); null si no aplica") String detalle
) {
    public static ElementoMarcadoResponse fromEntity(EjecucionElementoEntity m) {
        return new ElementoMarcadoResponse(m.getElemento().getId(), m.getElemento().getNombre(),
                m.getConjunto() != null ? m.getConjunto().getId() : null,
                m.getConjunto() != null ? m.getConjunto().getNombre() : null,
                m.getDetalle());
    }
}
