package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Lo que tiene adentro un conjunto (Configuración), en el orden en que se muestra. Reemplaza la lista
 * completa: lo que no venga se quita del conjunto (no se borra: las visitas viejas lo siguen usando).
 */
public record ConjuntoElementosRequest(
        @Schema(description = "Elementos del conjunto, en orden") @NotNull @Valid List<Item> elementos
) {
    /** Un elemento existente (elementoId) o uno nuevo por nombre (se reutiliza si ya existe escrito igual). */
    public record Item(
            @Schema(description = "ID de un elemento existente; null para crear o reutilizar por nombre", example = "4")
            Long elementoId,
            @Schema(description = "Nombre, cuando elementoId es null", example = "Fusibles") @Size(max = 200) String nombre,
            @Schema(description = "Al marcarlo se escribe cómo se encuentra", example = "false") Boolean pideDetalle
    ) {
    }
}
