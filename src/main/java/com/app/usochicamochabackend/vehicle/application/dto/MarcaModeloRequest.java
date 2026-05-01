package com.app.usochicamochabackend.vehicle.application.dto;

import jakarta.validation.constraints.NotBlank;

public record MarcaModeloRequest(
        @NotBlank(message = "La descripción de la marca es obligatoria")
        String descripcion
) {
}
