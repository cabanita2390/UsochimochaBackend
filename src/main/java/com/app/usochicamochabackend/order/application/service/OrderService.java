package com.app.usochicamochabackend.order.application.service;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.order.application.dto.AssignOrderRequest;
import com.app.usochicamochabackend.order.application.port.*;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService implements
        AssignOrderUseCase,
        FindAllOrdersUseCase,
        FindOrderByIdUseCase {

    private final OrderRepository orderRepository;
    private final InspectionRepository inspectionRepository;
    private final UserRepositoryJpa userRepository;

    @Override
    public OrderEntity assignOrder(AssignOrderRequest assignOrderRequest) {
        InspectionEntity inspectionEntity = inspectionRepository.findById(assignOrderRequest.inspectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Inspection not found with ID: " + assignOrderRequest.inspectionId()));

        UserEntity assignerUser = userRepository.findById(assignOrderRequest.assignerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigner user not found with ID: " + assignOrderRequest.assignerUserId()));

        UserEntity assignedUser = userRepository.findById(assignOrderRequest.assignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found with ID: " + assignOrderRequest.assignedUserId()));

        orderRepository.save(new OrderEntity());

        return null;
    }

    @Override
    public List<OrderEntity> findAllOrder() {
        return orderRepository.findAll();
    }

    @Override
    public OrderEntity findOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }
}