package com.app.usochicamochabackend.tire.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "LecturaPresionLlantaRequest",
        description = "Presión medida en una llanta. El servidor evalúa contra el perfil vigente del activo; "
                + "etiqueta y rango de referencia solo se usan si la posición ya no existe en el perfil "
                + "(p. ej. el móvil tenía una configuración anterior en caché).")
public record LecturaPresionLlantaRequest(
        @Schema(example = "TRA") String codigoPosicion,
        @Schema(description = "PSI medidos", example = "22") BigDecimal psi,
        @Schema(description = "Opcional: etiqueta que vio el operario") String etiquetaPosicion,
        @Schema(description = "Opcional: mínimo que usó el móvil") BigDecimal psiMinReferencia,
        @Schema(description = "Opcional: máximo que usó el móvil") BigDecimal psiMaxReferencia) {
}
