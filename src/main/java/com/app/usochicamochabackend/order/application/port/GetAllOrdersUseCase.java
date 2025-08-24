package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.GetAllOrdersDTO;

public interface GetAllOrdersUseCase {
    GetAllOrdersDTO getAllOrders();
}
