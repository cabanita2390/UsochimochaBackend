package com.app.usochicamochabackend.machine.application.port;

import com.app.usochicamochabackend.machine.application.dto.MachineRequest;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;

public interface CreateMachineUseCase {
    MachineResponse createMachine(MachineRequest machineRequest);
}
