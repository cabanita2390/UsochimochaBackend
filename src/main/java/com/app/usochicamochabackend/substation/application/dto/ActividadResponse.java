package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import io.swagger.v3.oas.annotations.media.Schema;

public record ActividadResponse(
        @Schema(description = "ID de la actividad", example = "1") Long id,
        @Schema(description = "Nombre de la actividad", example = "Pintura muros Estaciones (segun estado)") String nombre,
        @Schema(description = "Disciplina", example = "CIVIL") String disciplina,
        @Schema(description = "Captura móvil habilitada", example = "true") Boolean capturaMovilHabilitada,
        @Schema(description = "Citas del año actual (columna \"Citas 2026\")", example = "12") Integer citasPublicadasAnio,
        @Schema(description = "Activa", example = "true") Boolean activa,
        @Schema(description = "Nombre corto para la grilla del Cronograma; null si no tiene", example = "Pintura muros") String nombreCorto,
        @Schema(description = "Tiene citas o ejecuciones: su disciplina ya no se puede cambiar", example = "true") Boolean enUso,
        @Schema(description = "Solo al desactivar: citas futuras que quedaron para quitar al publicar; null en los demás casos", example = "3") Integer citasRetiradas
) {
    public static ActividadResponse fromEntity(ActividadEntity entity, Integer citasPublicadasAnio) {
        return fromEntity(entity, citasPublicadasAnio, null, null);
    }

    public static ActividadResponse fromEntity(ActividadEntity entity, Integer citasPublicadasAnio, Boolean enUso,
            Integer citasRetiradas) {
        return new ActividadResponse(entity.getId(), entity.getNombre(), entity.getDisciplina().getCodigo(),
                entity.getCapturaMovilHabilitada(), citasPublicadasAnio, entity.getStatus(), entity.getNombreCorto(),
                enUso, citasRetiradas);
    }
}
