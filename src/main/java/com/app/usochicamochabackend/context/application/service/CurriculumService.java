package com.app.usochicamochabackend.context.application.service;

import com.app.usochicamochabackend.context.application.dto.MachineCurriculumDTO;
import com.app.usochicamochabackend.context.application.port.GetMachineCurriculumUseCase;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.application.port.FindMachineByIdUseCase;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.mapper.ResultMapper;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.performance.application.dto.ResultDTO;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurriculumService implements GetMachineCurriculumUseCase {

    private final MachineRepository machineRepository;
    private final InspectionRepository inspectionRepository;
    private final FindMachineByIdUseCase findMachineByIdUseCase;

    @Override
    public MachineCurriculumDTO getMachineCurriculum(Long machineId) {
        MachineResponse machine = findMachineByIdUseCase.findMachineById(machineId);
        List<InspectionEntity> inspectionEntities = inspectionRepository.findByMachineId(machineId);
        List<OrderEntity> orderEntities = inspectionEntities.stream().map(InspectionEntity::getOrder).toList();
        List<ResultEntity> resultEntities = orderEntities.stream().map(OrderEntity::getResult).toList();
        List<ResultDTO> resultDTOS = ResultMapper.toResponseList(resultEntities);

        return new MachineCurriculumDTO(machine, resultDTOS);
    }
}