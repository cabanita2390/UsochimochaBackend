package com.app.usochicamochabackend.vehicle.application.service;

import com.app.usochicamochabackend.mapper.VehicleMapper;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleRequest;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;
import com.app.usochicamochabackend.vehicle.application.port.VehicleUseCase;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService implements VehicleUseCase {

    private final VehicleRepository vehicleRepository;

    @Override
    public List<VehicleResponse> findAllVehicles() {
        return vehicleRepository.findAllActiveVehicles().stream()
                .map(VehicleMapper::toResponse)
                .toList();
    }

    @Override
    public VehicleResponse findByPlaca(String placa) {
        return vehicleRepository.findVehicleDetailByPlaca(placa)
                .map(VehicleMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehículo no encontrado con placa: " + placa));
    }

    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {
        if (vehicleRepository.findByPlaca(request.placa()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un vehículo con esta placa");
        }

        VehicleEntity entity = VehicleEntity.builder()
                .placa(request.placa())
                .idMarca(request.idMarca())
                .idTipoVehiculo(request.idTipoVehiculo())
                .kilometrajeActual(request.kilometrajeActual())
                .belongsTo(request.belongsTo())
                .activo(true)
                .build();

        vehicleRepository.save(entity);
        return findByPlaca(entity.getPlaca());
    }

    @Override
    public VehicleResponse updateVehicle(Integer id, VehicleRequest request) {
        VehicleEntity entity = vehicleRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehículo no encontrado"));

        if (!entity.getPlaca().equals(request.placa()) && vehicleRepository.findByPlaca(request.placa()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe otro vehículo con esta placa");
        }

        entity.setPlaca(request.placa());
        entity.setIdMarca(request.idMarca());
        entity.setIdTipoVehiculo(request.idTipoVehiculo());
        entity.setKilometrajeActual(request.kilometrajeActual());
        entity.setBelongsTo(request.belongsTo());
        entity.setActivo(request.activo() != null ? request.activo() : entity.getActivo());

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
