package com.app.usochicamochabackend.vehicle.application.port;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleCreateRequest;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;

public interface CreateVehicleUseCase {
    VehicleResponse createVehicle(VehicleCreateRequest request);
}
