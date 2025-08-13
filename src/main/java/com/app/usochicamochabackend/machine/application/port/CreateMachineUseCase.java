package com.app.usochicamochabackend.machine.application.port;

import com.app.usochicamochabackend.machine.domain.model.Machine;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;

public interface CreateMachineUseCase {
    MachineEntity createMachine(MachineEntity machineEntity);
}
