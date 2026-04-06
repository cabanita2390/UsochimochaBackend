package com.app.usochicamochabackend.order.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;

public record OrderWithMachineDTO(OrderWithoutInspectionResponse order, MachineResponse machine) {}