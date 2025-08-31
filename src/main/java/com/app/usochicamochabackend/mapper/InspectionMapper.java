package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.dto.InspectionResponse;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;

public class InspectionMapper {

    private InspectionMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static InspectionEntity toEntity(InspectionFormRequest request,
                                            UserRepositoryJpa userRepository,
                                            MachineRepository machineRepository) {
        InspectionEntity entity = new InspectionEntity();

        entity.setUUID(request.UUID());
        entity.setDateStamp(request.dateStamp());
        entity.setUnexpected(request.isUnexpected());
        entity.setHourMeter(request.hourMeter());
        entity.setBrakeStatus(request.brakeStatus());
        entity.setLeakStatus(request.leakStatus());
        entity.setBeltsPulleysStatus(request.beltsPulleysStatus());
        entity.setTireLanesStatus(request.tireLanesStatus());
        entity.setCarIgnitionStatus(request.carIgnitionStatus());
        entity.setElectricalStatus(request.electricalStatus());
        entity.setMechanicalStatus(request.mechanicalStatus());
        entity.setTemperatureStatus(request.temperatureStatus());
        entity.setOilStatus(request.oilStatus());
        entity.setHydraulicStatus(request.hydraulicStatus());
        entity.setCoolantStatus(request.coolantStatus());
        entity.setStructuralStatus(request.structuralStatus());
        entity.setExpirationDateFireExtinguisher(request.expirationDateFireExtinguisher());
        entity.setGreasingAction(request.greasingAction());
        entity.setGreasingObservations(request.greasingObservations());
        entity.setObservations(request.observations());

        if (request.userId() != null) {
            entity.setUser(userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found")));
        }

        if (request.machineId() != null) {
            entity.setMachine(machineRepository.findById(request.machineId())
                    .orElseThrow(() -> new IllegalArgumentException("Machine not found")));
        }

        return entity;
    }

    public static InspectionResponse toDto(InspectionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new InspectionResponse(
                entity.getId(),
                entity.getUUID(),
                entity.getUnexpected(),
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
                entity.getUnexpected(),
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

    public static InspectionResponse toDtoWithoutImages(InspectionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new InspectionResponse(
                entity.getId(),
                entity.getUUID(),
                entity.getUnexpected(),
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
                null, // sin imágenes
                OrderMapper.toDtoWithoutInspection(entity.getOrder())
        );
    }
}
