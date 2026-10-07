package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.HallazgoSeguimientoEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SeguimientoResponse(
        @Schema(description = "Estado del hallazgo", example = "ABIERTO", allowableValues = {"ABIERTO", "EN_PROCESO", "RESUELTO"})
        String estado,
        @Schema(description = "Nombre de quien hizo el último cambio de estado", example = "Carlos Rivas") String actualizadoPor,
        LocalDateTime actualizadoEn,
        @Schema(description = "Nombre de quien lo resolvió (null si no está RESUELTO)", example = "Carlos Rivas") String cerradoPor,
        LocalDateTime cerradoEn,
        @Schema(description = "Qué se hizo para resolverlo", example = "Se selló la fisura con mortero epóxico.") String observacionesCierre,
        @Schema(description = "Ejecución posterior de la misma estación en la que se resolvió") Long resueltoEnEjecucionId,
        LocalDate resueltoEnEjecucionFecha,
        @Schema(description = "true si se resolvió en la misma visita en que se encontró") Boolean resueltoMismaVisita
) {
    public static SeguimientoResponse fromEntity(HallazgoSeguimientoEntity entity) {
        return new SeguimientoResponse(
                entity.getEstado(),
                nombre(entity.getActualizadoPor()),
                entity.getActualizadoEn(),
                nombre(entity.getCerradoPor()),
                entity.getCerradoEn(),
                entity.getObservacionesCierre(),
                entity.getResueltoEnEjecucion() != null ? entity.getResueltoEnEjecucion().getId() : null,
                entity.getResueltoEnEjecucion() != null ? entity.getResueltoEnEjecucion().getFecha() : null,
                entity.getResueltoMismaVisita());
    }

    /** Nombre completo; si el usuario no lo tiene, su username. */
    private static String nombre(UserEntity usuario) {
        if (usuario == null) {
            return null;
        }
        return usuario.getFullName() != null && !usuario.getFullName().isBlank()
                ? usuario.getFullName()
                : usuario.getUsername();
    }
}
