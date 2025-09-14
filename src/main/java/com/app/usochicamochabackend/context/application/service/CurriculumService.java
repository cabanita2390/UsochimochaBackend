package com.app.usochicamochabackend.context.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.context.application.dto.MachineCurriculumDTO;
import com.app.usochicamochabackend.context.application.port.GetMachineCurriculumUseCase;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.application.port.FindMachineByIdUseCase;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.mapper.MachineMapper;
import com.app.usochicamochabackend.mapper.ResultMapper;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.performance.application.dto.LaborResponse;
import com.app.usochicamochabackend.performance.application.dto.ResultDTO;
import com.app.usochicamochabackend.performance.application.dto.SparePartResponse;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SaveActionUseCase saveActionUseCase;
    private final NotificationService notificationService;

    @Override
    public MachineCurriculumDTO getMachineCurriculum(Long machineId) {
        MachineEntity machine = machineRepository.findById(machineId).orElseThrow(() ->  new ResourceNotFoundException("Machine not found"));
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

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() +
                " ha observado el curriculum de la maquina " + machine.getName());

        notificationService.notify("actions-updated");

        return new MachineCurriculumDTO(MachineMapper.toResponse(machine), resultDTOS, totalPrice);
    }
}
