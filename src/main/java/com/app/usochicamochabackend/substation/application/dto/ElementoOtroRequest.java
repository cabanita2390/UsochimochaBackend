package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * "Otro" en los elementos de un conjunto: el texto se guarda normalizado como un elemento de ese
 * conjunto (provisional, para revisión en Configuración) y el siguiente ya lo encuentra en la lista.
 */
public record ElementoOtroRequest(
        @Schema(description = "Conjunto donde se trabajó", example = "7") Long conjuntoId,
        @Schema(description = "Nombre del elemento, máx. 200", example = "Bornera auxiliar") String texto
) {
}
