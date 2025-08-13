package com.app.usochicamochabackend.machine.application.service;

import com.app.usochicamochabackend.machine.application.port.*;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MachineService implements FindMachineByIdUseCase, FindAllMachinesUseCase, DeleteMachineUseCase, CreateMachineUseCase, UpdateMachineUseCase {

    private final MachineRepository machineRepository;

    @Override
    public MachineEntity createMachine(MachineEntity machineEntity) {
        return machineRepository.save(machineEntity);
    }

    @Override
    public List<MachineEntity> findAllMachines() {
        return machineRepository.findAll().stream().toList();
    }

    @Override
    public MachineEntity findMachineById(Long id) {
        return  machineRepository.findById(id).orElse(null);
    }

    @Override
    public MachineEntity updateMachine(MachineEntity machineEntity) {
        return machineRepository.save(machineEntity);
    }

    @Override
    public void deleteMachine(Long id) {
        machineRepository.deleteById(id);
    }
}
