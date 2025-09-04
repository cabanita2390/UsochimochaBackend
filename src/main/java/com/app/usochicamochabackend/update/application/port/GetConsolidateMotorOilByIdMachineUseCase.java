package com.app.usochicamochabackend.update.application.port;

import com.app.usochicamochabackend.update.application.dto.ConsolidateMotorOilDTO;

public interface GetConsolidateMotorOilByIdMachineUseCase {
    ConsolidateMotorOilDTO getConsolidateMotorOilByIdMachine(Long idMachine);
}
