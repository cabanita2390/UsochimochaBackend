package com.app.usochicamochabackend.vehicle.application.dto;

public record VehicleFullResponse(
        Integer id,
        String placa,
        Integer idMarca,
        String marca,
        Integer idTipoVehiculo,
        String tipoVehiculo,
        Integer kilometrajeActual,
        Boolean activo
) {
}
