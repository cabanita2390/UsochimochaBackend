package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.GetAllOrdersByMachineId;

public interface GetAllOrdersByMachineIdUseCase {
    GetAllOrdersByMachineId getAllOrdersByMachineId(Long machineId);
}
