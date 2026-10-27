package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ElementoEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/** Conjunto o elemento del catálogo (formulario eléctrico, preguntas 9 y 11). */
public record ItemCatalogoResponse(
        @Schema(example = "3") Long id,
        @Schema(example = "Breaker") String nombre,
        @Schema(example = "ELECTRICO") String disciplina,
        @Schema(description = "Activo", example = "true") Boolean activa,
        @Schema(description = "Ya lo marcó alguna visita: su disciplina ya no se puede cambiar", example = "false") Boolean enUso
) {
    public static ItemCatalogoResponse fromEntity(ConjuntoEntity c, Boolean enUso) {
        return new ItemCatalogoResponse(c.getId(), c.getNombre(), c.getDisciplina().getCodigo(), c.getStatus(), enUso);
    }

    public static ItemCatalogoResponse fromEntity(ElementoEntity e, Boolean enUso) {
        return new ItemCatalogoResponse(e.getId(), e.getNombre(), e.getDisciplina().getCodigo(), e.getStatus(), enUso);
    }
}
