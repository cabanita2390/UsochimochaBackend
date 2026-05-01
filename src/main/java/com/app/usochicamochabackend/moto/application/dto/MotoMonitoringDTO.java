package com.app.usochicamochabackend.moto.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MotoMonitoringDTO(
    String departamento,
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
