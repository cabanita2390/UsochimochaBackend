package com.app.usochicamochabackend.moto.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MotoMonitoringDTO(
    String departamento,
    /** Ubicación por defecto del vehículo (catálogo {@code id_ubicacion_base}). */
    String ubicacionBase,
    /**
     * Unidad/sitio de la última inspección (nombre de {@code cat_ubicaciones} vía {@code insp_pre_operativa.id_ubicacion}).
     * El nombre del campo en JSON se mantiene por compatibilidad con clientes existentes.
     */
    String responsable,
    String placa,
    Integer kmActual,
    DocumentStatus soat,
    DocumentStatus tecno,
    OilStatus oil,
    String estadoMoto,
    LocalDateTime fechaUltimoReporte,
    Long diasUltimoReporte,
    String novedadActual
) {
    public record DocumentStatus(
        LocalDate fechaVencimiento,
        Long diasRestantes,
        String estado
    ) {}

    public record OilStatus(
        LocalDate fechaUltimoCambio,
        Integer kmCambio,
        Integer kmProximoCambio,
        Integer kmParaProximo,
        Boolean filtroAire,
        String estado
    ) {}
}
