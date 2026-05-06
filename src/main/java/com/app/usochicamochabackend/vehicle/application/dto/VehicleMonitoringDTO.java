package com.app.usochicamochabackend.vehicle.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(
        name = "VehicleMonitoringDTO",
        description = "Fila del tablero de monitoreo vehicular (sin motos): documentos, aceite y último reporte.")
public record VehicleMonitoringDTO(
        @Schema(description = "Placa del vehículo") String placa,
        @Schema(description = "Área / distrito o etiqueta organizacional") String area,
        @Schema(description = "Kilometraje actual en BD") Integer kmActual,
        @Schema(description = "Fecha/hora del último reporte de inspección u odómetro asociado") LocalDateTime fechaUltimoReporte,
        @Schema(description = "Días transcurridos desde el último reporte (si aplica)") Long diasUltimoReporte,
        @Schema(description = "Estado calculado del SOAT") DocumentStatus soat,
        @Schema(description = "Estado calculado de tecnomecánica") DocumentStatus tecno,
        @Schema(description = "Estado del mantenimiento de aceite") OilStatus maintenance
) {
    @Schema(
            name = "VehicleMonitoringDocumentStatus",
            description = "Vigencia y estado operativo de un documento (vehículos).")
    public record DocumentStatus(
            @Schema(description = "Fecha fin de vigencia") LocalDate fechaVencimiento,
            @Schema(description = "Días hasta vencimiento (negativo si ya venció)") Long diasRestantes,
            @Schema(description = "Vigente | Próximo a Vencer | Vencido") String estado
    ) {}

    @Schema(
            name = "VehicleMonitoringOilStatus",
            description = "Resumen del último cambio de aceite e intervalo.")
    public record OilStatus(
            @Schema(description = "Nombre o tipo de aceite") String tipoAceite,
            @Schema(description = "Fecha del último cambio registrado") LocalDate fechaUltimoCambio,
            @Schema(description = "Km en que se registró el cambio") Integer kmUltimoCambio,
            @Schema(description = "Km objetivo del próximo cambio") Integer kmProximoCambio,
            @Schema(description = "Km restantes hasta el próximo cambio") Integer kmParaCambio,
            @Schema(description = "Estado operativo del aceite (p. ej. OK, atención)") String estado
    ) {}
}
