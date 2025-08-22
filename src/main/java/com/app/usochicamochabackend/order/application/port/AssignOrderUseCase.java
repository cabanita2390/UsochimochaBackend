package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.AssignOrderRequest;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;

public interface AssignOrderUseCase {
    OrderEntity assignOrder(AssignOrderRequest assignOrderRequest);
}