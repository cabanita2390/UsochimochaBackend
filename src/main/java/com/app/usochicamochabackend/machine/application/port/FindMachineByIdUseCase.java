package com.app.usochicamochabackend.machine.application.port;

import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import org.springframework.security.core.userdetails.User;

public interface FindMachineByIdUseCase {
    MachineEntity findMachineById(Long id);
}
