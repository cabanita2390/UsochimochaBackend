package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Alta o edición de un conjunto o de un elemento (Configuración). */
public record ItemCatalogoRequest(
        @Schema(description = "Nombre (único por disciplina)", example = "Variador de velocidad")
        @NotBlank @Size(max = 200) String nombre,

        @Schema(description = "Código de disciplina existente en mant_disciplina", example = "ELECTRICO")
        @NotBlank String disciplina,

        @Schema(description = "Solo conjuntos: código del ícono del móvil (uno de ICONOS); null = genérico. Los elementos lo ignoran.",
                example = "GABINETE")
        String icono
) {
    /** Íconos que conocen la web y el móvil. Agregar uno exige dibujarlo en los dos. */
    public static final Set<String> ICONOS = Set.of(
            "SUBESTACION", "GABINETE", "RED_MT", "GENERADOR", "ILUMINACION", "MOTOR",
            "TRANSFORMADOR", "POSTE", "SENSOR", "CONTROL", "ENERGIA");

    /** Sin ícono (crear y editar elementos, clientes anteriores). */
    public ItemCatalogoRequest(String nombre, String disciplina) {
        this(nombre, disciplina, null);
    }
}
