package com.app.usochicamochabackend.moto.application.dto;

public record InspeccionMotoRequest(
        // Cabecera Principal y Detalles Vehiculo
        Integer idVehiculo,
        Integer kilometrajeReportado,
        String estadoVehiculo, // estado de la moto
        String observacionesFinales,

        // Detalle Documentos
        String checkSoat,
        String checkTecno,
        String checkLicencia,
        String checkExtintor,

        // Ubicación (Migrado a General)
        Integer idUbicacion) {
}
