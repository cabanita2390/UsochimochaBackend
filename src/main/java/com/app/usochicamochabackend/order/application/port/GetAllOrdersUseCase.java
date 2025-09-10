package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.OrderWithMachineDTO;
import com.app.usochicamochabackend.order.application.dto.OrdersWithMachinesResponse;

import java.util.List;

public interface GetAllOrdersUseCase {
     List<OrderWithMachineDTO> getAllOrders();
}
