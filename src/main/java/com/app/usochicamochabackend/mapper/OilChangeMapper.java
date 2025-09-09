package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.update.application.dto.PerformChangeHydraulicOilRequest;
import com.app.usochicamochabackend.update.application.dto.PerformChangeHydraulicOilResponse;
import com.app.usochicamochabackend.update.application.dto.PerformChangeMotorOilRequest;
import com.app.usochicamochabackend.update.application.dto.PerformChangeMotorOilResponse;
import com.app.usochicamochabackend.update.infrastructure.entity.OilChangeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

public final class OilChangeMapper {
    public static OilChangeEntity motorOilRequestToEntity(PerformChangeMotorOilRequest request, MachineRepository machineRepository) {
        MachineEntity machine = machineRepository.findById(request.machineId()).orElseThrow(() -> new ResourceNotFoundException("Machine not found with id: " + request.machineId()));
        OilChangeEntity entity = new OilChangeEntity();

        entity.setMotorOil(true);
        entity.setDateStamp(request.dateTime());
        entity.setBrand(request.brand());
        entity.setQuantity(request.quantity());
        entity.setHourMeter(request.currentHourMeter());
        entity.setAverageHoursChange(request.averageHoursChange());
        entity.setMachine(machine);

        return entity;
    }

    public static PerformChangeMotorOilResponse motorOilEntityToResponse(OilChangeEntity entity) {
        return  new PerformChangeMotorOilResponse(
                entity.getId(),
                MachineMapper.toResponse(entity.getMachine()),
                entity.getDateStamp(),
                entity.getBrand(),
                entity.getQuantity(),
                entity.getHourMeter(),
                entity.getAverageHoursChange()
        );
    }

    public static OilChangeEntity hydraulicOilRequestToEntity(PerformChangeHydraulicOilRequest request, MachineRepository machineRepository) {
        MachineEntity machine = MachineMapper.idToEntity(request.machineId(), machineRepository);
        OilChangeEntity entity = new OilChangeEntity();

        entity.setHydraulicOil(true);
        entity.setDateStamp(request.dateTime());
        entity.setBrand(request.brand());
        entity.setQuantity(request.quantity());
        entity.setHourMeter(request.currentHourMeter());
        entity.setAverageHoursChange(request.averageHoursChange());
        entity.setMachine(machine);

        return entity;
    }

    public static PerformChangeHydraulicOilResponse hydraulicOilEntityToResponse(OilChangeEntity entity) {
        return new PerformChangeHydraulicOilResponse(
                entity.getId(),
                MachineMapper.toResponse(entity.getMachine()),
                entity.getBrand(),
                entity.getHourMeter(),
                entity.getQuantity(),
                entity.getAverageHoursChange()
        );
    }
}
