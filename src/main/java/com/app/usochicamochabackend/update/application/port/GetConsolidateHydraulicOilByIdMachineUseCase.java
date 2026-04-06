package com.app.usochicamochabackend.update.application.port;

import com.app.usochicamochabackend.update.application.dto.ConsolidateHydraulicOilDTO;

public interface GetConsolidateHydraulicOilByIdMachineUseCase {
    ConsolidateHydraulicOilDTO getConsolidateHydraulicOilByIdMachine(Long idMachine);
}
