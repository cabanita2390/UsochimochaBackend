package com.app.usochicamochabackend.vehicle.application.port;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleUpdateRequest;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;

public interface UpdateVehicleUseCase {
    VehicleResponse updateVehicle(Integer id, VehicleUpdateRequest request);
}
