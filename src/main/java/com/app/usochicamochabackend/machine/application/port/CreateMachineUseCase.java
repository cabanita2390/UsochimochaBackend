package com.app.usochicamochabackend.machine.application.port;

import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;

public interface CreateMachineUseCase {
    MachineEntity createMachine(MachineEntity machineEntity);
}
