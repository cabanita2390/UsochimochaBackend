package com.app.usochicamochabackend.order.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.mapper.OrderMapper;
import com.app.usochicamochabackend.order.application.dto.AssignOrderRequest;
import com.app.usochicamochabackend.order.application.dto.OrderDTO;
import com.app.usochicamochabackend.order.application.port.AssignOrderUseCase;
import com.app.usochicamochabackend.order.application.port.GetOrderByIdUseCase;
import com.app.usochicamochabackend.order.application.port.GetOrderByInspectionId;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService implements AssignOrderUseCase, GetOrderByInspectionId, GetOrderByIdUseCase {

    private final OrderRepository orderRepository;
    private final InspectionRepository inspectionRepository;
    private final UserRepositoryJpa userRepository;
    private final SaveActionUseCase saveActionUseCase;

    @Override
    public OrderDTO assignOrder(AssignOrderRequest assignOrderRequest) {
        InspectionEntity inspectionEntity = inspectionRepository.findById(assignOrderRequest.inspectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Inspection not found with ID: " + assignOrderRequest.inspectionId()));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long assignerUserId = userPrincipal.id();

        UserEntity assignerUser = userRepository.findById(assignerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigner user not found with ID: " + assignerUserId));

        /*
        UserEntity assignedUser =  userRepository.findById(assignOrderRequest.assignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigner user not found with ID: " + assignOrderRequest.assignedUserId()));
         */
        OrderEntity orderEntity = orderRepository.save(
                OrderEntity.builder()
                        .status("Pending")
                        .description(assignOrderRequest.description())
                        .assignerUser(assignerUser)
                        //   .assignedUser(assignedUser)
                        .inspection(inspectionEntity)
                        .build()
        );

        saveActionUseCase.save("El usuario " + assignerUser.getUsername() +
                " ha asignado una orden de trabajo a la inspección al usuario *******");

        return OrderMapper.toDto(orderEntity);
    }

    @Override
    public OrderDTO getOrderByInspectionId(Long inspectionId) {
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

    @Override
    public OrderDTO getOrderById(Long orderId) {
        return OrderMapper.toDto(orderRepository.findById(orderId).orElseThrow(()->new ResourceNotFoundException("Order not found with ID: " + orderId)));
    }
}
