package com.app.usochicamochabackend.update.web;

import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.order.application.dto.GetAllOrdersByMachineId;
import com.app.usochicamochabackend.order.application.port.GetAllOrdersByMachineIdUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.usochicamochabackend.update.application.dto.*;
import com.app.usochicamochabackend.update.application.port.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/oil-changes")
@RequiredArgsConstructor
@Tag(name = "Oil Changes", description = "Operations related to motor and hydraulic oil changes")
public class OilChangeController {

    private final GetConsolidateMotorOilByIdMachineUseCase getConsolidateMotorOilByIdMachine;
    private final GetConsolidateMotorOilAllMachinesUseCase getConsolidateMotorOilAllMachines;
    private final GetConsolidateHydraulicOilByIdMachineUseCase getConsolidateHydraulicOilByIdMachine;
    private final GetConsolidateHydraulicOilAllMachines getConsolidateHydraulicOilAllMachines;
    private final GetConsolidateHydraulicAndMotorOilByIdMachineUseCase getConsolidateHydraulicAndMotorOilByIdMachine;
    private final GetConsolidateHydraulicAndMotorOilAllMachinesUseCase getConsolidateHydraulicAndMotorOilAllMachines;
    private final PerformMotorOilChangeUseCase performMotorOilChange;
    private final PerformHydraulicChangeUseCase performHydraulicChange;

    @GetMapping("/motor/{machineId}")
    @Operation(summary = "Get motor oil consolidation for a machine")
    public ConsolidateMotorOilDTO getMotorOilByMachine(@PathVariable Long machineId) {
        return getConsolidateMotorOilByIdMachine.getConsolidateMotorOilByIdMachine(machineId);
    }

    @GetMapping("/motor")
    @Operation(summary = "Get motor oil consolidation for all machines")
    public List<ConsolidateMotorOilDTO> getAllMotorOil() {
        return getConsolidateMotorOilAllMachines.getConsolidateMotorOilAll();
    }

    @GetMapping("/hydraulic/{machineId}")
    @Operation(summary = "Get hydraulic oil consolidation for a machine")
    public ConsolidateHydraulicOilDTO getHydraulicOilByMachine(@PathVariable Long machineId) {
        return getConsolidateHydraulicOilByIdMachine.getConsolidateHydraulicOilByIdMachine(machineId);
    }

    @GetMapping("/hydraulic")
    @Operation(summary = "Get hydraulic oil consolidation for all machines")
    public List<ConsolidateHydraulicOilDTO> getAllHydraulicOil() {
        return getConsolidateHydraulicOilAllMachines.getConsolidateHydraulicOilAllMachines();
    }

    @GetMapping("/consolidated/{machineId}")
    @Operation(summary = "Get consolidated motor and hydraulic oil for a machine")
    public ConsolidateHydraulicAndMotorOilDTO getConsolidatedByMachine(@PathVariable Long machineId) {
        return getConsolidateHydraulicAndMotorOilByIdMachine.getConsolidateHydraulicAndMotorOilById(machineId);
    }

    @GetMapping("/consolidated")
    @Operation(summary = "Get consolidated motor and hydraulic oil for all machines")
    public List<ConsolidateHydraulicAndMotorOilDTO> getAllConsolidated() {
        return getConsolidateHydraulicAndMotorOilAllMachines.getConsolidateHydraulicAndMotorOilAllMachines();
    }

    @PostMapping("/motor")
    @Operation(summary = "Register a motor oil change")
    public PerformChangeMotorOilResponse performMotorOilChange(@RequestBody PerformChangeMotorOilRequest request) {
        return performMotorOilChange.performMotorOilChange(request);
    }

    @PostMapping("/hydraulic")
    @Operation(summary = "Register a hydraulic oil change")
    public PerformChangeHydraulicOilResponse performHydraulicOilChange(@RequestBody PerformChangeHydraulicOilRequest request) {
        return performHydraulicChange.performChangeHydraulicOil(request);
    }


}