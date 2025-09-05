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

        // Reutilizamos los métodos que ya implementaste
        ConsolidateMotorOilDTO motorOil = getConsolidateMotorOilByIdMachine(machineId);
        ConsolidateHydraulicOilDTO hydraulicOil = getConsolidateHydraulicOilByIdMachine(machineId);

        // Creamos "versiones limpias" de motor y hidráulico (sin currentData)
        ConsolidateMotorOilDTO cleanMotorOil = new ConsolidateMotorOilDTO(
                null, // currentData vacío
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
                null, // currentData vacío
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

        List<OilChangeEntity> oilChanges = oilChangeRepository.getTwoLastHydraulicOilChangesByMachineId(machineId);
        if (oilChanges.isEmpty()) {
            throw new ResourceNotFoundException("No hydraulic oil changes found for machine " + machineId);
        }

        OilChangeEntity last = oilChanges.get(0);

        int averageChangeHours = last.getAverageHoursChange();
        int hourMeterLastUpdate = last.getHourMeter();
        int hourMeterNextUpdate = hourMeterLastUpdate + averageChangeHours;

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                last.getDateStamp().toLocalDate(),
                java.time.LocalDate.now()
        );

        int timeLastUpdateMouths = (int) (daysBetween / 30);
        int remainingHoursNextUpdateMouths = hourMeterNextUpdate - hourMeterLastUpdate;

        return new ConsolidateHydraulicOilDTO(
                currentData,
                "HYDRAULIC",
                last.getBrand(),
                last.getQuantity(),
                averageChangeHours,
                last.getDateStamp().toLocalDate(),
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
        System.out.println("lastInspection = " + lastInspection);

        CurrentData currentData = new CurrentData(
                machine.getBelongsTo(),
                machine.getName(),
                lastInspection != null ? lastInspection.getHourMeter() : null,
                lastInspection != null ? lastInspection.getDateStamp() : null
        );

        List<OilChangeEntity> oilChanges = oilChangeRepository.getTwoLastMotorOilChangesByMachineId(machineId);
        if (oilChanges.isEmpty()) {
            throw new ResourceNotFoundException("No motor oil changes found for machine " + machineId);
        }

        OilChangeEntity last = oilChanges.get(0); // más reciente

        // Validar que tenga fecha
        if (last.getDateStamp() == null) {
            throw new IllegalStateException("OilChangeEntity with id " + last.getId() + " has no dateStamp");
        }

        int averageChangeHours = last.getAverageHoursChange();
        int hourMeterLastUpdate = last.getHourMeter();
        int hourMeterNextUpdate = hourMeterLastUpdate + averageChangeHours;

        // Usamos LocalDate para el cálculo de días
        LocalDate lastDate = last.getDateStamp().toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(lastDate, LocalDate.now());

        int timeLastUpdateMonths = (int) (daysBetween / 30);
        int remainingHoursNextUpdate = hourMeterNextUpdate - hourMeterLastUpdate;

        return new ConsolidateMotorOilDTO(
                currentData,
                last.getId(),
                "MOTOR",
                last.getBrand(),
                last.getQuantity(),
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
    public PerformChangeHydraulicOilResponse performChangeHydraulicOil(PerformChangeMotorOilRequest request) {
        OilChangeEntity oilChange = OilChangeMapper.motorOilRequestToEntity(request, machineRepository);

        oilChangeRepository.save(oilChange);

        return OilChangeMapper.hydraulicOilEntityToResponse(oilChange);
    }
}
