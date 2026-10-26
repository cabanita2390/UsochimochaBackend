package com.app.usochicamochabackend.tire.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "ConfigLlantasActivoResponse",
        description = "Perfil de llantas vigente de un activo (exactamente uno de vehicleId / machineId). "
                + "El móvil lo guarda en caché para pedir y evaluar la presión sin conexión.")
public record ConfigLlantasActivoResponse(
        Integer vehicleId,
        Long machineId,
        Long perfilId,
        String perfilNombre,
        String aplicaA,
        List<PosicionLlantaResponse> posiciones) {
}
