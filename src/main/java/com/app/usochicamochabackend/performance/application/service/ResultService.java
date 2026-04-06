package com.app.usochicamochabackend.performance.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.mapper.ResultMapper;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.order.application.dto.OrderResponse;
import com.app.usochicamochabackend.order.application.port.GetOrderByIdUseCase;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderRequest;
import com.app.usochicamochabackend.performance.application.dto.ExecuteDTO;
import com.app.usochicamochabackend.performance.application.port.*;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.performance.infrastructure.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResultService implements ExecuteAnOrderUseCase {

    private final ResultRepository resultRepository;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final OrderRepository orderRepository;
    private final UserRepositoryJpa  userRepository;
    private final SaveActionUseCase saveActionUseCase;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ExecuteDTO execute(ExecuteAnOrderRequest request) {
        OrderResponse orderResponse = getOrderByIdUseCase.getOrderById(request.orderId());
        OrderEntity orderEntity = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (orderEntity.getResult() != null) {
            throw new RuntimeException("Order has already been executed");
        }

        ResultEntity result = ResultMapper.toEntity(request, orderEntity, userRepository);
        ResultEntity savedResult = resultRepository.save(result);

        orderEntity.setResult(savedResult);
        orderEntity.setStatus("Done");
        orderRepository.save(orderEntity);

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() + " ha ejecutado una orden de trabajo asignada a la inspeccion realizada a la maquina " + orderEntity.getInspection().getMachine().getName() + " el dia " + orderEntity.getInspection().getDateStamp());


        return ResultMapper.toResponse(savedResult, orderResponse);
    }

}