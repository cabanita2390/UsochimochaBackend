package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.OrderWithMachineDTO;
import com.app.usochicamochabackend.order.application.dto.OrdersWithMachinesResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GetAllOrdersUseCase {
     Page<OrderWithMachineDTO> getAllOrders(Pageable pageable);
}
