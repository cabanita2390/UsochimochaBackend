package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Resultado de asignar o copiar: lo creado y lo omitido (no falla por duplicados ni meses cerrados). */
public record AsignacionResultado(
        @Schema(description = "Citas nuevas en BORRADOR", example = "8") Integer creadas,
        @Schema(description = "Ya existían (misma estación, actividad, mes y año)", example = "1") Integer omitidasDuplicadas,
        @Schema(description = "Meses cerrados (anteriores al actual)", example = "0") Integer omitidasMesCerrado,
        @Schema(description = "Estaciones inactivas", example = "0") Integer omitidasEstacionInactiva
) {
}
