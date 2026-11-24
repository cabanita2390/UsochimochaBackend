package com.app.usochicamochabackend.common.excel.reporte;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Un filtro de un reporte. {@code tipo} le dice a la web cómo pedirlo:
 * DISCIPLINA, ESTACION, ANIO, MES (el usuario lo elige en el panel) o CONTEXTO (lo pone la
 * pantalla desde donde se descarga, p. ej. el tipo de activo o el activo, y no se muestra).
 */
public record FiltroReporte(
        @Schema(example = "disciplina") String clave,
        @Schema(example = "Disciplina") String etiqueta,
        @Schema(example = "DISCIPLINA") String tipo,
        @Schema(description = "Sin él el reporte responde 400") boolean obligatorio
) {
    public static FiltroReporte elegible(String clave, String etiqueta, String tipo) {
        return new FiltroReporte(clave, etiqueta, tipo, false);
    }

    public static FiltroReporte contexto(String clave, String etiqueta, boolean obligatorio) {
        return new FiltroReporte(clave, etiqueta, "CONTEXTO", obligatorio);
    }
}
