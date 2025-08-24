package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;

public class MachineMapper {

    private MachineMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static MachineResponse toResponse(MachineEntity entity) {
        if (entity == null) {
            return null;
        }

        return new MachineResponse(
                entity.getId(),
                entity.getName(),
                entity.getModel(),
                entity.getSoat(),
                entity.getBrand(),
                entity.getRunt(),
                entity.getNumEngine(),
                entity.getNumInterIdentification()
        );
    }
}