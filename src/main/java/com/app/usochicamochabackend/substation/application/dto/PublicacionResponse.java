package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.PublicacionEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Una entrada del historial de publicaciones del año. */
public record PublicacionResponse(
        @Schema(example = "5") Long id,
        @Schema(example = "2026") Integer anio,
        @Schema(example = "2026-10-20T10:48:00") LocalDateTime publicadoEn,
        @Schema(description = "\"Carga inicial\" para la publicación inicial", example = "Laura Méndez") String usuario,
        @Schema(example = "5") Integer altas,
        @Schema(example = "1") Integer bajas,
        Boolean inicial,
        Boolean revertida,
        @Schema(description = "Quién la deshizo; null si sigue vigente") String revertidaPor,
        LocalDateTime revertidaEn
) {
    public static final String CARGA_INICIAL = "Carga inicial";

    public static PublicacionResponse fromEntity(PublicacionEntity p) {
        return new PublicacionResponse(
                p.getId(), p.getAnio(), p.getPublicadoEn(),
                p.getInicial() || p.getUsuario() == null ? CARGA_INICIAL : p.getUsuario().getFullName(),
                p.getAltas(), p.getBajas(), p.getInicial(), p.getRevertida(),
                p.getRevertidaPor() != null ? p.getRevertidaPor().getFullName() : null,
                p.getRevertidaEn());
    }
}
