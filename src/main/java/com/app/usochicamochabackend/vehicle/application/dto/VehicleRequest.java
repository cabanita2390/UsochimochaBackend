package com.app.usochicamochabackend.vehicle.application.dto;

public record VehicleRequest(String placa, Integer idMarca, Integer idTipoVehiculo, Integer kilometrajeActual, Boolean activo) {
}
