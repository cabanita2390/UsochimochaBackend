package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.AssignOrderRequest;
import com.app.usochicamochabackend.order.application.dto.OrderResponse;

public interface AssignOrderUseCase {
    OrderResponse assignOrder(AssignOrderRequest assignOrderRequest);
}