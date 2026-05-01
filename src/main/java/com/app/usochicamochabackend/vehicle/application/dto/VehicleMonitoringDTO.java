package com.app.usochicamochabackend.vehicle.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VehicleMonitoringDTO(
    String placa,
    String area,
    Integer kmActual,
    LocalDateTime fechaUltimoReporte,
    Long diasUltimoReporte,
    DocumentStatus soat,
    DocumentStatus tecno,
    OilStatus maintenance
) {
    public record DocumentStatus(
        LocalDate fechaVencimiento,
        Long diasRestantes,
        String estado
    ) {}

    public record OilStatus(
        String tipoAceite,
        LocalDate fechaUltimoCambio,
        Integer kmUltimoCambio,
        Integer kmProximoCambio,
        Integer kmParaCambio,
        String estado
    ) {}
}
