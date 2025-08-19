package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;

import java.util.List;

public interface FindAllOrdersUseCase {
    List<OrderEntity> findAllOrder();
}
