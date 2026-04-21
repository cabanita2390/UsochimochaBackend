package com.app.usochicamochabackend.vehicle.application.service;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;
import com.app.usochicamochabackend.vehicle.application.port.FindAllVehiclesUseCase;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService implements FindAllVehiclesUseCase {

    private final VehicleRepository vehicleRepository;

    @Override
    public List<VehicleResponse> findAllVehicles() {
        return vehicleRepository.findAllActiveVehicles().stream()
                .map(v -> new VehicleResponse(v.getId(), v.getPlaca(), v.getMarca(), v.getTipoVehiculo(),
                        v.getKilometrajeActual()))
                .toList();
    }

    @Override
    public VehicleResponse findByPlaca(String placa) {
        return vehicleRepository.findVehicleDetailByPlaca(placa)
                .map(v -> new VehicleResponse(v.getId(), v.getPlaca(), v.getMarca(), v.getTipoVehiculo(),
                        v.getKilometrajeActual()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehículo no encontrado con placa: " + placa));
    }
}
