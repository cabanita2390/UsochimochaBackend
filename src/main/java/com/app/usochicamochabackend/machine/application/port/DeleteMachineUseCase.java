package com.app.usochicamochabackend.machine.application.port;

import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;

public interface DeleteMachineUseCase {
    void deleteMachine(Long id);
}
