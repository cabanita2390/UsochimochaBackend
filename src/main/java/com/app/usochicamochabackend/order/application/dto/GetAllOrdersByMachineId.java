package com.app.usochicamochabackend.order.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;

import java.util.List;

public record GetAllOrdersByMachineId(MachineResponse machine, List<OrderWithoutInspectionResponse> orders) {}