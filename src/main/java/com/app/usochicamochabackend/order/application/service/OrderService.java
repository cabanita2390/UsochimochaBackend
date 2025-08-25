package com.app.usochicamochabackend.order.application.service;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.mapper.OrderMapper;
import com.app.usochicamochabackend.order.application.dto.AssignOrderRequest;
import com.app.usochicamochabackend.order.application.dto.AssignOrderResponse;
import com.app.usochicamochabackend.order.application.port.AssignOrderUseCase;
import com.app.usochicamochabackend.order.application.port.GetOrderByInspectionId;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService implements AssignOrderUseCase, GetOrderByInspectionId {

    private final OrderRepository orderRepository;
    private final InspectionRepository inspectionRepository;
    private final UserRepositoryJpa userRepository;

    @Override
    public AssignOrderResponse assignOrder(AssignOrderRequest assignOrderRequest) {
        InspectionEntity inspectionEntity = inspectionRepository.findById(assignOrderRequest.inspectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Inspection not found with ID: " + assignOrderRequest.inspectionId()));

        UserEntity assignerUser = userRepository.findById(assignOrderRequest.assignerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigner user not found with ID: " + assignOrderRequest.assignerUserId()));

        UserEntity inspectionUser = inspectionEntity.getUser();
        MachineEntity inspectionMachine = inspectionEntity.getMachine();

        OrderEntity orderEntity = orderRepository.save(
                OrderEntity.builder()
                        .status("Pending")
                        .description(assignOrderRequest.description())
                        .assignerUser(assignerUser)
                        .assignedUser(inspectionUser)
                        .inspection(inspectionEntity)
                        .build()
        );

        return OrderMapper.toDto(orderEntity);
    }

    @Override
    public AssignOrderResponse getOrderByInspectionId(Long inspectionId) {
        InspectionEntity inspectionEntity = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inspection not found with ID: " + inspectionId));

        OrderEntity orderEntity = inspectionEntity.getOrder();
        if (orderEntity == null) {
            throw new ResourceNotFoundException(
                    "No order found for Inspection ID: " + inspectionId);
        }

        return OrderMapper.toDto(orderEntity);
    }
}
