package com.app.usochicamochabackend.vehicle.application.service;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;
import com.app.usochicamochabackend.vehicle.application.port.FindAllVehiclesUseCase;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleCreateRequest;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleUpdateRequest;
import com.app.usochicamochabackend.vehicle.application.port.CreateVehicleUseCase;
import com.app.usochicamochabackend.vehicle.application.port.DeleteVehicleUseCase;
import com.app.usochicamochabackend.vehicle.application.port.UpdateVehicleUseCase;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VehicleService implements FindAllVehiclesUseCase, CreateVehicleUseCase, UpdateVehicleUseCase, DeleteVehicleUseCase {

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
    @Override
    public VehicleResponse createVehicle(VehicleCreateRequest request) {
        if (vehicleRepository.findByPlaca(request.placa()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un vehículo con esta placa");
        }

        VehicleEntity entity = VehicleEntity.builder()
                .placa(request.placa())
                .idMarca(request.idMarca())
                .idTipoVehiculo(request.idTipoVehiculo())
                .kilometrajeActual(request.kilometrajeActual())
                .activo(true)
                .build();

        vehicleRepository.save(entity);
        return findByPlaca(entity.getPlaca());
    }

    @Override
    public VehicleResponse updateVehicle(Integer id, VehicleUpdateRequest request) {
        VehicleEntity entity = vehicleRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehículo no encontrado"));

        if (!entity.getPlaca().equals(request.placa()) && vehicleRepository.findByPlaca(request.placa()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe otro vehículo con esta placa");
        }

        entity.setPlaca(request.placa());
        entity.setIdMarca(request.idMarca());
        entity.setIdTipoVehiculo(request.idTipoVehiculo());
        entity.setKilometrajeActual(request.kilometrajeActual());
        entity.setActivo(request.activo());

        vehicleRepository.save(entity);
        return findByPlaca(entity.getPlaca());
    }

    @Override
    public void deleteVehicle(Integer id) {
        if (!vehicleRepository.existsById(Long.valueOf(id))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehículo no encontrado");
        }
        vehicleRepository.deleteById(Long.valueOf(id));
    }
}
