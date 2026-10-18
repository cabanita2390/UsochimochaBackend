package com.app.usochicamochabackend.substation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Texto obligatorio de cerrar (observación), devolver (motivo) o anular (motivo). */
public record ComentarioOrdenRequest(
        @Schema(description = "Observación de la verificación o motivo", example = "Se revisó en sitio: la fisura quedó sellada.")
        String comentario
) {
}
