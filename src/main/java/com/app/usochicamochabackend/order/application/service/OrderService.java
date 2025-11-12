package com.app.usochicamochabackend.order.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.mapper.InspectionMapper;
import com.app.usochicamochabackend.mapper.MachineMapper;
import com.app.usochicamochabackend.mapper.OrderMapper;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.order.application.dto.*;
import com.app.usochicamochabackend.order.application.port.AssignOrderUseCase;
import com.app.usochicamochabackend.order.application.port.GetAllOrdersByInspectionIdUseCase;
import com.app.usochicamochabackend.order.application.port.GetAllOrdersByMachineIdUseCase;
import com.app.usochicamochabackend.order.application.port.GetAllOrdersUseCase;
import com.app.usochicamochabackend.order.application.port.GetOrderByIdUseCase;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements AssignOrderUseCase, GetAllOrdersByInspectionIdUseCase, GetOrderByIdUseCase, GetAllOrdersUseCase, GetAllOrdersByMachineIdUseCase {

    private final OrderRepository orderRepository;
    private final MachineRepository machineRepository;
    private final InspectionRepository inspectionRepository;
    private final UserRepositoryJpa userRepository;
    private final SaveActionUseCase saveActionUseCase;
    private final NotificationService notificationService;

    @Override
    public OrderResponse assignOrder(AssignOrderRequest assignOrderRequest) {
        InspectionEntity inspectionEntity = inspectionRepository.findById(assignOrderRequest.inspectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Inspection not found with ID: " + assignOrderRequest.inspectionId()));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long assignerUserId = userPrincipal.id();

        UserEntity assignerUser = userRepository.findById(assignerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigner user not found with ID: " + assignerUserId));

        OrderEntity orderEntity = orderRepository.save(
                OrderEntity.builder()
                        .status("Pending")
                        .description(assignOrderRequest.description())
                        .assignerUser(assignerUser)
                        .inspection(inspectionEntity)
                        .build()
        );

        saveActionUseCase.save("El usuario " + assignerUser.getUsername() +
                " ha asignado una orden de trabajo a la inspección realizada a la maquina " + inspectionEntity.getMachine().getName() + " el dia " + orderEntity.getInspection().getDateStamp());


        return OrderMapper.toDto(orderEntity);
    }

    @Override
    public GetAllOrdersByInspectionIdResponse getAllOrdersByInspectionId(Long inspectionId) {
        InspectionEntity inspectionEntity = inspectionRepository.findById(inspectionId).orElseThrow(() -> new ResourceNotFoundException("Inspection not found with ID: " + inspectionId));

        List<OrderEntity> orders = orderRepository.getAllByInspectionId(inspectionId);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found");
        }

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() +
                " ha observado todas la ordenes de trabajo asigandas a la inspeccion que se le realizo a la maquina " + inspectionEntity.getMachine().getName() + " el dia " + inspectionEntity.getDateStamp().toLocalDate());


        return new GetAllOrdersByInspectionIdResponse(InspectionMapper.toDto(inspectionEntity), orders.stream().map(OrderMapper::toDto).toList());
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {

        return OrderMapper.toDto(orderRepository.findById(orderId).orElseThrow(()->new ResourceNotFoundException("Order not found with ID: " + orderId)));
    }

    @Override
    public Page<OrderWithMachineDTO> getAllOrders(Pageable pageable) {
        Page<OrderEntity> orders = orderRepository.findAll(pageable);

        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found");
        }

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() +
                " ha observado todas la ordenes de trabajo asigandas");


        return orders.map(order -> {
                    OrderWithoutInspectionResponse orderDTO = OrderMapper.toDtoWithoutInspection(order);
                    MachineResponse machineDTO = MachineMapper.toResponse(order.getInspection().getMachine());
                    return new OrderWithMachineDTO(orderDTO, machineDTO);
                });
    }

    @Override
    public GetAllOrdersByMachineId getAllOrdersByMachineId(Long machineId) {
        MachineEntity machineEntity = machineRepository.findById(machineId).orElseThrow(()->new ResourceNotFoundException("Machine not found with ID: " + machineId));

        List<InspectionEntity> inspections = inspectionRepository.findByMachineId(machineId);

        if (inspections.isEmpty()) {
            throw new ResourceNotFoundException("No inspections found");
        }

        List<OrderEntity> orders = inspections.stream()
                .flatMap(inspection -> inspection.getOrders().stream())
                .toList();

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() +
                " ha observado todas la ordenes de trabajo asigandas a la maquina " + machineEntity.getName());


        return new GetAllOrdersByMachineId(MachineMapper.toResponse(machineEntity), OrderMapper.toDtoListWithoutInspection(orders));
    }
}
