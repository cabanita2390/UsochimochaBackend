package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Orden de trabajo en la lista "Prioritarias" del móvil: lo que hay que hacer y las fotos del hallazgo. */
public record OrdenMovilResponse(
        OrdenResumenResponse orden,
        @Schema(description = "Fotos de la visita en que se encontró el hallazgo (ANTES)") List<EvidenciaResponse> evidenciasHallazgo,
        @Schema(description = "Si el supervisor la devolvió, por qué; null si no") String motivoDevolucion
) {
}
