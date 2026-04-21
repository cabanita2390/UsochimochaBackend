package com.app.usochicamochabackend.vehicle.application.port;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;

import java.util.List;

public interface FindAllVehiclesUseCase {
    List<VehicleResponse> findAllVehicles();

    VehicleResponse findByPlaca(String placa);
}
