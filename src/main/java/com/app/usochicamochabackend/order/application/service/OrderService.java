package com.app.usochicamochabackend.order.application.service;

import com.app.usochicamochabackend.order.application.port.*;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements
        CreateOrderUseCase,
        DeleteOrderUseCase,
        FindAllOrdersUseCase,
        FindOrderByIdUseCase,
        UpdateOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    public OrderEntity createOrder(OrderEntity orderEntity) {
        return orderRepository.save(orderEntity);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderEntity> findAllOrder() {
        return orderRepository.findAll();
    }

    @Override
    public OrderEntity findOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public OrderEntity updateOrder(OrderEntity orderEntity) {
        return orderRepository.save(orderEntity);
    }
}