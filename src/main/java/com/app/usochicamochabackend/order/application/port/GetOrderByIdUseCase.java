package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.OrderResponse;

public interface GetOrderByIdUseCase {
    OrderResponse getOrderById(Long orderId);
}
