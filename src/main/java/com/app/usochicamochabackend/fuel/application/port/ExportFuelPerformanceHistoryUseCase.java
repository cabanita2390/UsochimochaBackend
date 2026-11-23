package com.app.usochicamochabackend.fuel.application.port;

public interface ExportFuelPerformanceHistoryUseCase {
    // tipo: MAQUINARIA | VEHICULO | MOTOCICLETA (igual que GetFuelPerformanceUseCase);
    // activoId: machineId si tipo=MAQUINARIA, vehicleId en los otros dos casos.
    default byte[] exportarHistorialActivo(String tipo, Long activoId) {
        return exportarHistorialActivo(tipo, activoId, null, null);
    }

    /** Con periodo (desde / hasta, ambos opcionales): sin fechas, todo el historial. */
    byte[] exportarHistorialActivo(String tipo, Long activoId, java.time.LocalDate desde, java.time.LocalDate hasta);
}
