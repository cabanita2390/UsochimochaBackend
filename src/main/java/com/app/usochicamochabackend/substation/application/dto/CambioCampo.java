package com.app.usochicamochabackend.substation.application.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Un campo que cambió en una edición de ejecución, se guarda como JSON en mant_ejecucion_edicion.cambios. */
public record CambioCampo(
        @Schema(description = "Campo editado", example = "resultado") String campo,
        @Schema(description = "Valor anterior (null si estaba vacío)", example = "CONFORME") String antes,
        @Schema(description = "Valor nuevo (null si quedó vacío)", example = "CON_HALLAZGOS") String despues
) {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<List<CambioCampo>> LISTA = new TypeReference<>() {
    };

    public static String aJson(List<CambioCampo> cambios) {
        try {
            return JSON.writeValueAsString(cambios);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar la lista de cambios", e);
        }
    }

    /** null si la edición es anterior a V45 (no guardaba cambios). */
    public static List<CambioCampo> desdeJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return JSON.readValue(json, LISTA);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("cambios mal formado en mant_ejecucion_edicion: " + json, e);
        }
    }
}
