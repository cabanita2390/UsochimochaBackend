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
import com.app.usochicamochabackend.performance.application.dto.LaborResponse;
import com.app.usochicamochabackend.performance.application.dto.ResultDTO;
import com.app.usochicamochabackend.performance.application.dto.SparePartResponse;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        if (inspectionEntities.isEmpty()) {
            throw new ResourceNotFoundException("No inspections found");
        }

        List<ResultEntity> resultEntities = inspectionEntities.stream()
                .flatMap(i -> i.getOrders().stream())
                .map(OrderEntity::getResult)
                .filter(Objects::nonNull)
                .toList();

        List<ResultDTO> resultDTOS = ResultMapper.toResponseList(resultEntities);

        BigDecimal totalPrice = resultDTOS.stream()
                .flatMap(result -> Stream.concat(
                        Stream.ofNullable(result.labor()).map(LaborResponse::price),
                        result.spareParts().stream().map(SparePartResponse::price)
                ))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new MachineCurriculumDTO(machine, resultDTOS, totalPrice);
    }
}
