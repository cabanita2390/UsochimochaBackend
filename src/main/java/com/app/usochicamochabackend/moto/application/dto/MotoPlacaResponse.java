package com.app.usochicamochabackend.moto.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Referencia mínima para listas de trabajo: id y placa de moto activa.")
public record MotoPlacaResponse(
        @Schema(description = "`vehiculos.id_vehiculo`", example = "4") Integer id,
        @Schema(description = "Placa", example = "MOT001") String placa) {
}
