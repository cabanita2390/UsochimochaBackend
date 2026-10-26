package com.app.usochicamochabackend.tire.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "PerfilLlantasRequest", description = "Alta o edición de un perfil de llantas.")
public record PerfilLlantasRequest(
        @Schema(example = "Volqueta 6x4 (10 llantas)") String nombre,
        @Schema(description = "VEHICULO | MOTOCICLETA | MAQUINARIA", example = "VEHICULO") String aplicaA,
        String descripcion,
        List<PosicionLlantaRequest> posiciones) {
}
