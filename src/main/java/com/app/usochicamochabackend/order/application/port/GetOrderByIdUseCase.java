package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.OrderDTO;

public interface GetOrderByIdUseCase {
    OrderDTO getOrderById(Long orderId);
}
