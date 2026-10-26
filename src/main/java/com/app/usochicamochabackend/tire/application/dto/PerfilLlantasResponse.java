package com.app.usochicamochabackend.tire.application.dto;

import java.util.List;

public record PerfilLlantasResponse(
        Long id,
        String nombre,
        String aplicaA,
        String descripcion,
        List<PosicionLlantaResponse> posiciones) {
}
