package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.review.application.dto.InspectionResponse;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;

public class InspectionMapper {

    private InspectionMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static InspectionResponse toDto(InspectionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new InspectionResponse(
                entity.getId(),
                entity.getUUID(),
                entity.getIsUnexpected(),
                entity.getDateStamp(),
                entity.getHourMeter(),
                entity.getLeakStatus(),
                entity.getBrakeStatus(),
                entity.getBeltsPulleysStatus(),
                entity.getTireLanesStatus(),
                entity.getCarIgnitionStatus(),
                entity.getElectricalStatus(),
                entity.getMechanicalStatus(),
                entity.getTemperatureStatus(),
                entity.getOilStatus(),
                entity.getHydraulicStatus(),
                entity.getCoolantStatus(),
                entity.getStructuralStatus(),
                entity.getExpirationDateFireExtinguisher(),
                entity.getGreasingAction(),
                entity.getGreasingObservations(),
                entity.getObservations(),
                UserMapper.toResponse(entity.getUser()),
                MachineMapper.toResponse(entity.getMachine()),
                ImagesMapper.toDtoList(entity.getImages()),
                OrderMapper.toDtoWithoutInspection(entity.getOrder())
        );
    }

    public static InspectionResponse toDtoWithoutOrder(InspectionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new InspectionResponse(
                entity.getId(),
                entity.getUUID(),
                entity.getIsUnexpected(),
                entity.getDateStamp(),
                entity.getHourMeter(),
                entity.getLeakStatus(),
                entity.getBrakeStatus(),
                entity.getBeltsPulleysStatus(),
                entity.getTireLanesStatus(),
                entity.getCarIgnitionStatus(),
                entity.getElectricalStatus(),
                entity.getMechanicalStatus(),
                entity.getTemperatureStatus(),
                entity.getOilStatus(),
                entity.getHydraulicStatus(),
                entity.getCoolantStatus(),
                entity.getStructuralStatus(),
                entity.getExpirationDateFireExtinguisher(),
                entity.getGreasingAction(),
                entity.getGreasingObservations(),
                entity.getObservations(),
                UserMapper.toResponse(entity.getUser()),
                MachineMapper.toResponse(entity.getMachine()),
                ImagesMapper.toDtoList(entity.getImages()),
                null
        );
    }
}
