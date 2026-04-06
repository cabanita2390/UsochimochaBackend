package com.app.usochicamochabackend.update.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;

public record ConsolidateHydraulicAndMotorOilDTO(MachineResponse machine, CurrentData currentData, ConsolidateMotorOilDTO  consolidateMotorOil, ConsolidateHydraulicOilDTO consolidateHydraulicOil) {}