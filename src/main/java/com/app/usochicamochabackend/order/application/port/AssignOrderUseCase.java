package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.AssignOrderRequest;
import com.app.usochicamochabackend.order.application.dto.AssignOrderResponse;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;

public interface AssignOrderUseCase {
    AssignOrderResponse assignOrder(AssignOrderRequest assignOrderRequest);
}