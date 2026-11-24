package com.app.usochicamochabackend.common.excel.reporte;

import com.app.usochicamochabackend.common.excel.Periodo;
import com.app.usochicamochabackend.exception.BadRequestException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Lo que se pidió: periodo, filtros (clave → valor), columnas elegidas y quién descarga. */
public record PeticionReporte(Periodo periodo, Map<String, String> filtros, List<String> columnas, String usuario) {

    public PeticionReporte {
        periodo = periodo == null ? Periodo.todo() : periodo;
        // Sin los vacíos: un filtro que no vino y uno en blanco son lo mismo.
        filtros = filtros == null ? Map.of() : filtros.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null && !e.getValue().isBlank())
                .collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().trim()));
        columnas = columnas == null ? List.of() : List.copyOf(columnas);
    }

    public static PeticionReporte de(LocalDate desde, LocalDate hasta, Map<String, String> filtros, String usuario) {
        return new PeticionReporte(new Periodo(desde, hasta), filtros, null, usuario);
    }

    /** El filtro o null si no vino (o vino vacío). */
    public String filtro(String clave) {
        String v = filtros.get(clave);
        return v == null || v.isBlank() ? null : v.trim();
    }

    public Long filtroLong(String clave) {
        String v = filtro(clave);
        try {
            return v == null ? null : Long.valueOf(v);
        } catch (NumberFormatException e) {
            throw new BadRequestException(clave + " debe ser un número: " + v);
        }
    }

    public Integer filtroEntero(String clave) {
        Long v = filtroLong(clave);
        return v == null ? null : v.intValue();
    }

    public String filtroObligatorio(String clave) {
        String v = filtro(clave);
        if (v == null) {
            throw new BadRequestException("Falta el filtro " + clave);
        }
        return v;
    }
}
