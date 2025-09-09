package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.mapper.MachineMapper;
import com.app.usochicamochabackend.mapper.OilChangeMapper;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.update.application.dto.*;
import com.app.usochicamochabackend.update.application.port.*;
import com.app.usochicamochabackend.update.infrastructure.entity.OilChangeEntity;
import com.app.usochicamochabackend.update.infrastructure.repository.OilChangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OilChangeService implements
        PerformMotorOilChangeUseCase,
        PerformHydraulicChangeUseCase,
        GetConsolidateMotorOilByIdMachineUseCase,
        GetConsolidateHydraulicOilByIdMachineUseCase,
        GetConsolidateMotorOilAllMachinesUseCase,
        GetConsolidateHydraulicOilAllMachines,
        GetConsolidateHydraulicAndMotorOilByIdMachineUseCase,
        GetConsolidateHydraulicAndMotorOilAllMachinesUseCase
{

    private final MachineRepository machineRepository;
    private final InspectionRepository inspectionRepository;
    private final OilChangeRepository oilChangeRepository;

    @Override
    public List<ConsolidateHydraulicAndMotorOilDTO> getConsolidateHydraulicAndMotorOilAllMachines() {
        return machineRepository.findAll().stream()
                .map(machine -> getConsolidateHydraulicAndMotorOilById(machine.getId()))
                .toList();
    }


    @Override
    public ConsolidateHydraulicAndMotorOilDTO getConsolidateHydraulicAndMotorOilById(Long machineId) {
        MachineEntity machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found with id " + machineId));

        InspectionEntity lastInspection = inspectionRepository.getLastInspection(machineId);
        CurrentData currentData = new CurrentData(
                machine.getBelongsTo(),
                machine.getName(),
                lastInspection.getHourMeter(),
                lastInspection.getDateStamp()
        );

        ConsolidateMotorOilDTO motorOil = getConsolidateMotorOilByIdMachine(machineId);
        ConsolidateHydraulicOilDTO hydraulicOil = getConsolidateHydraulicOilByIdMachine(machineId);

        ConsolidateMotorOilDTO cleanMotorOil = new ConsolidateMotorOilDTO(
                null,
                motorOil.id(),
                motorOil.type(),
                motorOil.brand(),
                motorOil.quantity(),
                motorOil.averageChangeHours(),
                motorOil.dateLastUpdate(),
                motorOil.hourMeterLastUpdate(),
                motorOil.hourMeterNextUpdate(),
                motorOil.timeLastUpdateMouths(),
                motorOil.remainingHoursNextUpdateMouths()
        );

        ConsolidateHydraulicOilDTO cleanHydraulicOil = new ConsolidateHydraulicOilDTO(
                null,
                hydraulicOil.type(),
                hydraulicOil.brand(),
                hydraulicOil.quantity(),
                hydraulicOil.averageChangeHours(),
                hydraulicOil.dateLastUpdate(),
                hydraulicOil.hourMeterLastUpdate(),
                hydraulicOil.hourMeterNextUpdate(),
                hydraulicOil.timeLastUpdateMouths(),
                hydraulicOil.remainingHoursNextUpdateMouths()
        );

        return new ConsolidateHydraulicAndMotorOilDTO(
                MachineMapper.toResponse(machine),
                currentData,
                cleanMotorOil,
                cleanHydraulicOil
        );
    }

    @Override
    public List<ConsolidateHydraulicOilDTO> getConsolidateHydraulicOilAllMachines() {
        return machineRepository.findAll().stream()
                .map(machine -> getConsolidateHydraulicOilByIdMachine(machine.getId()))
                .toList();
    }

    @Override
    public ConsolidateHydraulicOilDTO getConsolidateHydraulicOilByIdMachine(Long machineId) {
        MachineEntity machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found with id " + machineId));

        InspectionEntity lastInspection = inspectionRepository.getLastInspection(machineId);
        CurrentData currentData = new CurrentData(
                machine.getBelongsTo(),
                machine.getName(),
                lastInspection.getHourMeter(),
                lastInspection.getDateStamp()
        );

        OilChangeEntity oilLastChange = oilChangeRepository.getLastHydraulicOilChangeByMachineId(machineId);

        int averageChangeHours = oilLastChange.getAverageHoursChange();
        int hourMeterLastUpdate = oilLastChange.getHourMeter();
        int hourMeterNextUpdate = hourMeterLastUpdate + averageChangeHours;

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                oilLastChange.getDateStamp().toLocalDate(),
                java.time.LocalDate.now()
        );

        int timeLastUpdateMouths = (int) (daysBetween / 30);
        int remainingHoursNextUpdateMouths = hourMeterNextUpdate - hourMeterLastUpdate;

        return new ConsolidateHydraulicOilDTO(
                currentData,
                "HYDRAULIC",
                oilLastChange.getBrand(),
                oilLastChange.getQuantity(),
                averageChangeHours,
                oilLastChange.getDateStamp().toLocalDate(),
                hourMeterLastUpdate,
                hourMeterNextUpdate,
                timeLastUpdateMouths,
                remainingHoursNextUpdateMouths
        );
    }

    @Override
    public List<ConsolidateMotorOilDTO> getConsolidateMotorOilAll() {
        return machineRepository.findAll().stream()
                .map(machine -> getConsolidateMotorOilByIdMachine(machine.getId()))
                .toList();
    }

    @Override
    public ConsolidateMotorOilDTO getConsolidateMotorOilByIdMachine(Long machineId) {
        MachineEntity machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found with id " + machineId));

        InspectionEntity lastInspection = inspectionRepository.getLastInspection(machineId);

        CurrentData currentData = new CurrentData(
                machine.getBelongsTo(),
                machine.getName(),
                lastInspection != null ? lastInspection.getHourMeter() : null,
                lastInspection != null ? lastInspection.getDateStamp() : null
        );

        OilChangeEntity oilLastChange = oilChangeRepository.getLastMotorOilChangeByMachineId(machineId);

        if (oilLastChange.getDateStamp() == null) {
            throw new IllegalStateException("OilChangeEntity with id " + oilLastChange.getId() + " has no dateStamp");
        }

        int averageChangeHours = oilLastChange.getAverageHoursChange();
        int hourMeterLastUpdate = oilLastChange.getHourMeter();
        int hourMeterNextUpdate = hourMeterLastUpdate + averageChangeHours;

        LocalDate lastDate = oilLastChange.getDateStamp().toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(lastDate, LocalDate.now());

        int timeLastUpdateMonths = (int) (daysBetween / 30);
        int remainingHoursNextUpdate = hourMeterNextUpdate - hourMeterLastUpdate;

        return new ConsolidateMotorOilDTO(
                currentData,
                oilLastChange.getId(),
                "MOTOR",
                oilLastChange.getBrand(),
                oilLastChange.getQuantity(),
                averageChangeHours,
                lastDate,
                hourMeterLastUpdate,
                hourMeterNextUpdate,
                timeLastUpdateMonths,
                remainingHoursNextUpdate
        );
    }



    @Override
    public PerformChangeMotorOilResponse performMotorOilChange(PerformChangeMotorOilRequest request) {
        OilChangeEntity oilChange = OilChangeMapper.motorOilRequestToEntity(request, machineRepository);

        oilChangeRepository.save(oilChange);

        return OilChangeMapper.motorOilEntityToResponse(oilChange);
    }

    @Override
    public PerformChangeHydraulicOilResponse performChangeHydraulicOil(PerformChangeHydraulicOilRequest request) {
        OilChangeEntity oilChange = OilChangeMapper.hydraulicOilRequestToEntity(request, machineRepository);

        oilChangeRepository.save(oilChange);

        return OilChangeMapper.hydraulicOilEntityToResponse(oilChange);
    }
}
