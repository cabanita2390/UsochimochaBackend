package com.app.usochicamochabackend.performance.application.service;

import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.mapper.ResultMapper;
import com.app.usochicamochabackend.order.application.dto.OrderDTO;
import com.app.usochicamochabackend.order.application.port.GetOrderByIdUseCase;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderRequest;
import com.app.usochicamochabackend.performance.application.dto.ExecuteDTO;
import com.app.usochicamochabackend.performance.application.port.*;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.performance.infrastructure.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResultService implements ExecuteAnOrderUseCase {

    private final ResultRepository resultRepository;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public ExecuteDTO execute(ExecuteAnOrderRequest request) {
        OrderDTO orderDTO = getOrderByIdUseCase.getOrderById(request.orderId());
        OrderEntity orderEntity = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (orderEntity.getResult() != null) {
            throw new RuntimeException("Order has already been executed");
        }

        ResultEntity result = ResultMapper.toEntity(request, orderEntity);

        ResultEntity savedResult = resultRepository.save(result);

        orderEntity.setResult(savedResult);
        orderRepository.save(orderEntity);

        return ResultMapper.toResponse(savedResult, orderDTO);
    }

}