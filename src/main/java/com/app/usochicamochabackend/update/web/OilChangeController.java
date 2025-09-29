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

    private final GetConsolidateHydraulicAndMotorOilAllMachinesUseCase getConsolidateHydraulicAndMotorOilAllMachines;
    private final PerformMotorOilChangeUseCase performMotorOilChange;
    private final PerformHydraulicChangeUseCase performHydraulicChange;

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