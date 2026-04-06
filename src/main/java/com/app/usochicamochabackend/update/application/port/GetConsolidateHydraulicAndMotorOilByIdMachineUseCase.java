package com.app.usochicamochabackend.update.application.port;

import com.app.usochicamochabackend.update.application.dto.ConsolidateHydraulicAndMotorOilDTO;

public interface GetConsolidateHydraulicAndMotorOilByIdMachineUseCase {
    ConsolidateHydraulicAndMotorOilDTO getConsolidateHydraulicAndMotorOilById(Long idMachine);
}
