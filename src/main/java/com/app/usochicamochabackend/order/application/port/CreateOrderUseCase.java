package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;

public interface CreateOrderUseCase {
    OrderEntity createOrder(OrderEntity orderEntity);
}