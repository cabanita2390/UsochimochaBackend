package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/** Una disciplina y lo que la web y el móvil deben hacer con ella. */
public record DisciplinaResponse(
        @Schema(example = "ELECTRICO") String codigo,
        @Schema(example = "Eléctrico") String nombre,
        @Schema(description = "Letra de las celdas del cronograma", example = "E") String sigla,
        @Schema(description = "Orden en listas y filtros", example = "2") Integer orden,
        @Schema(description = "El móvil permite registrar visitas de esta disciplina") Boolean capturaMovil,
        @Schema(description = "Conjuntos en \"¿Qué se intervino?\": NO, OPCIONAL u OBLIGATORIO", example = "OPCIONAL")
        String conjuntos,
        @Schema(description = "\"¿Qué se intervino?\" pide también elementos") Boolean usaElementos,
        @Schema(description = "La visita marca equipos (Electromecánico)") Boolean usaEquipos,
        @Schema(description = "La visita lleva controles de alineación, sellos y rodamientos (Electromecánico)")
        Boolean usaControles
) {
    public static DisciplinaResponse fromEntity(DisciplinaEntity d) {
        return new DisciplinaResponse(d.getCodigo(), d.getNombre() != null ? d.getNombre() : d.getCodigo(),
                d.getSigla() != null ? d.getSigla() : d.getCodigo().substring(0, 1),
                d.getOrden(), d.getCapturaMovil(), d.getConjuntos(), d.getUsaElementos(), d.getUsaEquipos(),
                d.getUsaControles());
    }
}
