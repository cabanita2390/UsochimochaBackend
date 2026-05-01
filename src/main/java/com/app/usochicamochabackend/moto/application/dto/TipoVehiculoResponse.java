package com.app.usochicamochabackend.moto.application.dto;

public record TipoVehiculoResponse(
        Integer id,
        String nombreTipo,
        Boolean activo
) {
}
