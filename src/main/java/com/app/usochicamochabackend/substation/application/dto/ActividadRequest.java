package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActividadRequest(
        @Schema(description = "Nombre de la actividad (único por disciplina)", example = "Pintura muros estaciones")
        @NotBlank @Size(max = 200) String nombre,

        @Schema(description = "Código de disciplina existente en mant_disciplina", example = "CIVIL")
        @NotBlank String disciplina,

        @Schema(description = "Si el técnico puede registrarla desde el móvil", example = "true")
        @NotNull Boolean capturaMovilHabilitada,
        @Schema(description = "Nombre corto para la grilla del Cronograma (opcional, máx. 24)", example = "Pintura muros")
        @Size(max = 24) String nombreCorto
) {
}
