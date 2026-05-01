package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleProjection;

public class VehicleMapper {

    public static VehicleResponse toResponse(VehicleEntity entity) {
        if (entity == null) return null;
        return new VehicleResponse(
                entity.getIdVehiculo(),
                entity.getPlaca(),
                entity.getMarca() != null ? entity.getMarca().getDescripcion() : null,
                entity.getTipoVehiculo() != null ? entity.getTipoVehiculo().getNombreTipo() : null,
                entity.getKilometrajeActual(),
                entity.getBelongsTo()
        );
    }

    public static VehicleResponse toResponse(VehicleProjection projection) {
        if (projection == null) return null;
        return new VehicleResponse(
                projection.getId(),
                projection.getPlaca(),
                projection.getMarca(),
                projection.getTipoVehiculo(),
                projection.getKilometrajeActual(),
                projection.getBelongsTo()
        );
    }
}
