package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;

public interface FindOrderByIdUseCase {
    OrderEntity findOrderById(Long id);
}
