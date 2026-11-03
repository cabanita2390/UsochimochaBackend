package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** Detalle de una orden: el hallazgo (antes), la visita que la corrigió (después) y la línea de tiempo. */
public record OrdenDetalleResponse(
        OrdenResumenResponse orden,
        @Schema(description = "La visita en que se encontró el hallazgo, con sus fotos (ANTES)") EjecucionResponse hallazgo,
        @Schema(description = "La visita del operario que la ejecutó, con sus fotos (DESPUÉS); null si todavía no") EjecucionResponse ejecucion,
        String observacionCierre,
        String verificadoPor,
        LocalDateTime verificadoEn,
        String motivoAnulacion,
        List<OrdenEventoResponse> eventos
) {
}
