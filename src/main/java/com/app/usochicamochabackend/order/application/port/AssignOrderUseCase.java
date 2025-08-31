package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.AssignOrderRequest;
import com.app.usochicamochabackend.order.application.dto.OrderDTO;

public interface AssignOrderUseCase {
    OrderDTO assignOrder(AssignOrderRequest assignOrderRequest);
}