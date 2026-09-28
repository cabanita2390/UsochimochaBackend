package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EstacionRequest(
        @Schema(description = "Nombre de la estación (único)", example = "Duitama")
        @NotBlank @Size(max = 120) String nombre,

        @Schema(description = "Tipo de estación", example = "BOMBEO", allowableValues = {"BOMBEO", "COMPLEMENTARIA"})
        @NotNull @Pattern(regexp = "BOMBEO|COMPLEMENTARIA", message = "debe ser BOMBEO o COMPLEMENTARIA") String tipo,

        @Schema(description = "Frecuencia base de mantenimiento", example = "TRIMESTRAL",
                allowableValues = {"MENSUAL", "BIMESTRAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL"})
        @NotNull @Pattern(regexp = "MENSUAL|BIMESTRAL|TRIMESTRAL|SEMESTRAL|ANUAL",
                message = "debe ser MENSUAL, BIMESTRAL, TRIMESTRAL, SEMESTRAL o ANUAL") String frecuenciaBase
) {
}
