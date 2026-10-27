package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Alta o edición de un conjunto o de un elemento (Configuración). */
public record ItemCatalogoRequest(
        @Schema(description = "Nombre (único por disciplina)", example = "Variador de velocidad")
        @NotBlank @Size(max = 200) String nombre,

        @Schema(description = "Código de disciplina existente en mant_disciplina", example = "ELECTRICO")
        @NotBlank String disciplina
) {
}
