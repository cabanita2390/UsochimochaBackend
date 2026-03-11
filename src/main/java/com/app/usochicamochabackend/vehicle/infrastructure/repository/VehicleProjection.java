package com.app.usochicamochabackend.vehicle.infrastructure.repository;

public interface VehicleProjection {
    Integer getId();

    String getPlaca();

    String getMarca();

    String getTipoVehiculo();

    Integer getKilometrajeActual();
}
