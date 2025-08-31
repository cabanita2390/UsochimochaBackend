package com.app.usochicamochabackend.machine.application.port;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;

import java.util.List;

public interface FindAllMachinesUseCase {
    List<MachineResponse> findAllMachines();
}
