package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.update.application.dto.VehicleOilChangeRequest;
import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;
import com.app.usochicamochabackend.update.infrastructure.entity.VehicleOilChangeEntity;
import com.app.usochicamochabackend.update.infrastructure.repository.BrandRepository;
import com.app.usochicamochabackend.update.infrastructure.repository.VehicleOilChangeRepository;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleOilChangeService {

    private final VehicleOilChangeRepository vehicleOilChangeRepository;
    private final VehicleRepository vehicleRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public void registerChange(VehicleOilChangeRequest request) {
        VehicleEntity vehicle = vehicleRepository.findByPlaca(request.placa())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));

        BrandEntity brand = null;
        if (request.brandId() != null) {
            brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new IllegalArgumentException("Marca de aceite no encontrada"));
        }

        VehicleOilChangeEntity entity = VehicleOilChangeEntity.builder()
                .vehicle(vehicle)
                .dateStamp(request.dateStamp() != null ? request.dateStamp() : java.time.LocalDateTime.now())
                .oilType(request.oilType())
                .brand(brand)
                .quantity(request.quantity())
                .kmAtChange(request.kmAtChange())
                .intervalKm(request.intervalKm())
                .airFilterChanged(request.airFilterChanged())
                .build();

        vehicleOilChangeRepository.save(entity);
        
        // Optionally update vehicle's current Km if the change happened at a higher Km
        if (request.kmAtChange() > (vehicle.getKilometrajeActual() != null ? vehicle.getKilometrajeActual() : 0)) {
            vehicle.setKilometrajeActual(request.kmAtChange());
            vehicleRepository.save(vehicle);
        }
    }
}
