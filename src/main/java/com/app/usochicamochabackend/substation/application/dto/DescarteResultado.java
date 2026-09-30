package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Resultado de "Descartar": qué se quitó del borrador y qué se conservó. */
public record DescarteResultado(
        @Schema(description = "Citas nuevas (borrador) dadas de baja", example = "5") Integer altasDescartadas,
        @Schema(description = "Citas marcadas para quitar que vuelven a quedar publicadas", example = "1") Integer retirosAnulados,
        @Schema(description = "Borradores que no se tocaron porque ya tienen ejecución", example = "0") Integer conservadasConEjecucion
) {
}
