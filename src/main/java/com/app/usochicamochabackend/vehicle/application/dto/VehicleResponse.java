package com.app.usochicamochabackend.vehicle.application.dto;

public record VehicleResponse(
        Integer id,
        String placa,
        String marca,
        String tipoVehiculo,
        Integer kilometrajeActual,
        String belongsTo,
        Integer idUbicacionBase,
        String ubicacionBase
) {
}
