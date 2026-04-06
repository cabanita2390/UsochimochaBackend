package com.app.usochicamochabackend.order.application.dto;

import java.util.List;

public record OrdersWithMachinesResponse(List<OrderWithMachineDTO> allOrders) {}