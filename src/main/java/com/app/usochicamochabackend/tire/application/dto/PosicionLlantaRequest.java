package com.app.usochicamochabackend.tire.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "PosicionLlantaRequest", description = "Una llanta del perfil con su rango de presión en PSI.")
public record PosicionLlantaRequest(
        @Schema(description = "Código único dentro del perfil (A-Z, 0-9, _)", example = "TRA") String codigo,
        @Schema(description = "Nombre visible para el operario", example = "Trasera") String etiqueta,
        @Schema(description = "Orden en el formulario; si falta se usa el de la lista") Integer orden,
        @Schema(description = "Presión mínima aceptable (PSI)", example = "28") BigDecimal psiMin,
        @Schema(description = "Presión recomendada (PSI), opcional", example = "30") BigDecimal psiObjetivo,
        @Schema(description = "Presión máxima aceptable (PSI)", example = "33") BigDecimal psiMax) {
}
