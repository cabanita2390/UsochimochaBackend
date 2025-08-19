package com.app.usochicamochabackend.machine.web;

import com.app.usochicamochabackend.machine.application.service.MachineService;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/machine")
@RequiredArgsConstructor
@Tag(name = "Machine", description = "Endpoints for managing machines")
public class MachineController {

    private final MachineService machineService;

    @GetMapping("/{id}")
    @Operation(summary = "Get machine by ID", description = "Returns a single machine by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Machine found"),
            @ApiResponse(responseCode = "404", description = "Machine not found")
    })
    public ResponseEntity<MachineEntity> getMachineById(@PathVariable Long id) {
        return ResponseEntity.ok(machineService.findMachineById(id));
    }

    @GetMapping
    @Operation(summary = "Get all machines", description = "Returns a list of all machines.")
    public ResponseEntity<List<MachineEntity>> getMachines() {
        return ResponseEntity.ok(machineService.findAllMachines());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete machine", description = "Deletes a machine by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Machine deleted"),
            @ApiResponse(responseCode = "404", description = "Machine not found")
    })
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    @Operation(summary = "Create machine", description = "Creates a new machine and returns its URI.")
    @ApiResponse(responseCode = "201", description = "Machine created")
    public ResponseEntity<MachineEntity> createMachine(@RequestBody MachineEntity machineEntity) throws URISyntaxException {
        MachineEntity machineSaved = machineService.createMachine(machineEntity);
        return ResponseEntity.created(new URI("/api/v1/machine/" + machineSaved.getId())).body(machineSaved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update machine", description = "Updates an existing machine and returns its new URI.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Machine updated"),
            @ApiResponse(responseCode = "404", description = "Machine not found")
    })
    public ResponseEntity<MachineEntity> updateMachine(@RequestBody MachineEntity machineEntity) throws URISyntaxException {
        MachineEntity machineSaved = machineService.updateMachine(machineEntity);
        return ResponseEntity.created(new URI("/api/v1/machine/" + machineSaved.getId())).body(machineSaved);
    }
}