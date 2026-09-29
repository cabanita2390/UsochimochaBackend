package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEdicionEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record EjecucionEdicionResponse(
        @Schema(description = "Usuario que hizo la edición") String usuario,
        @Schema(description = "Motivo de la edición") String motivo,
        @Schema(description = "Fecha/hora de la edición") LocalDateTime editadoEn,
        @Schema(description = "Campos que cambiaron, con su valor antes y después; null en ediciones anteriores a V45")
        List<CambioCampo> cambios
) {
    public static EjecucionEdicionResponse fromEntity(EjecucionEdicionEntity entity) {
        return new EjecucionEdicionResponse(
                entity.getUsuario().getUsername(),
                entity.getMotivo(),
                entity.getEditadoEn(),
                CambioCampo.desdeJson(entity.getCambios()));
    }
}
