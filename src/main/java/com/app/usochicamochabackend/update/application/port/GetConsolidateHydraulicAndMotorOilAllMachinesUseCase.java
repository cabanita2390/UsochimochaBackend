package com.app.usochicamochabackend.update.application.port;

import com.app.usochicamochabackend.update.application.dto.ConsolidateHydraulicAndMotorOilDTO;
import com.app.usochicamochabackend.update.application.dto.ConsolidateMotorOilDTO;

import java.util.List;

public interface GetConsolidateHydraulicAndMotorOilAllMachinesUseCase {
    List<ConsolidateHydraulicAndMotorOilDTO> getConsolidateHydraulicAndMotorOilAllMachines();
}
